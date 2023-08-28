package com.backpackcloud.sherlogholmes.domain.chart;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ChartBuilder<X, Y> {

  private final Function<DataEntry, Label<X>> labelFunction;
  private final Function<DataEntry, String> seriesFunction;
  private final Function<DataEntry, Y> dataPointFunction;
  private final Function<Collection<Y>, Y> reducerFunction;
  private final Supplier<Collection<Y>> collectionSupplier;

  public ChartBuilder(Function<DataEntry, Label<X>> labelFunction,
                      Function<DataEntry, String> seriesFunction,
                      Function<DataEntry, Y> dataPointFunction,
                      Function<Collection<Y>, Y> reducerFunction,
                      Supplier<Collection<Y>> collectionSupplier) {
    this.labelFunction = labelFunction;
    this.seriesFunction = seriesFunction;
    this.dataPointFunction = dataPointFunction;
    this.reducerFunction = reducerFunction;
    this.collectionSupplier = collectionSupplier;
  }

  public Chart<X, Y> build(Stream<DataEntry> data) {
    Map<String, Map<Label<X>, Collection<Y>>> seriesData = new HashMap<>();
    Set<Label<X>> labels = new TreeSet<>();

    data.forEach(entry -> {
      Label<X> label = labelFunction.apply(entry);
      labels.add(label);
      String series = seriesFunction.apply(entry);
      if (!seriesData.containsKey(series)) {
        seriesData.put(series, new TreeMap<>());
      }
      Map<Label<X>, Collection<Y>> labelSeries = seriesData.get(series);
      if (!labelSeries.containsKey(label)) {
        labelSeries.put(label, collectionSupplier.get());
      }
      labelSeries.get(label).add(dataPointFunction.apply(entry));
    });

    Map<String, Series<X, Y>> seriesMap = new HashMap<>();

    seriesData.forEach((seriesName, rawData) -> {
      List<DataPoint<X, Y>> dataPoints = new ArrayList<>();
      rawData.forEach((label, values) -> dataPoints.add(new DataPointImpl<>(label, reducerFunction.apply(values))));
      seriesMap.put(seriesName, new SeriesImpl<>(seriesName, labels.stream().toList(), dataPoints, reducerFunction));
    });

    return new ChartImpl<>(labels.stream().toList(), new ArrayList<>(seriesMap.values()));
  }

  private record SeriesImpl<X, Y>(String name,
                                  List<Label<X>> labels,
                                  List<DataPoint<X, Y>> data,
                                  Function<Collection<Y>, Y> reducerFunction) implements Series<X, Y> {

    @Override
    public Optional<DataPoint<X, Y>> at(Label<X> label) {
      return data.stream()
        .filter(p -> p.label().equals(label))
        .findFirst();
    }

    @Override
    public Series<X, Y> merge(String newName, Series<X, Y> other) {
      List<DataPoint<X, Y>> newData = new ArrayList<>();

      Iterator<DataPoint<X, Y>> lefIterator;
      Iterator<DataPoint<X, Y>> rightIterator;

      if (this.data.size() >= other.data().size()) {
        lefIterator = this.data.iterator();
        rightIterator = other.data().iterator();
      } else {
        lefIterator = other.data().iterator();
        rightIterator = this.data.iterator();
      }

      // assuming both series have the same labels
      while (lefIterator.hasNext()) {
        DataPoint<X, Y> left = lefIterator.next();
        if (rightIterator.hasNext()) {
          DataPoint<X, Y> right = rightIterator.next();
          newData.add(new DataPointImpl<>(left.label(), reducerFunction.apply(List.of(left.value(), right.value()))));
        } else {
          newData.add(left);
        }
      }

      return new SeriesImpl<>(name, labels, newData, reducerFunction);
    }

  }

  private record DataPointImpl<X, Y>(Label<X> label, Y value) implements DataPoint<X, Y> {

  }

  private record ChartImpl<X, Y>(List<Label<X>> labels, List<Series<X, Y>> series) implements Chart<X, Y> {

    @Override
    public List<Series<X, Y>> series(int maxSize) {
      if (series.size() <= maxSize) {
        return series;
      }
      List<Series<X, Y>> result = new ArrayList<>(series.subList(0, maxSize));
      Iterator<Series<X, Y>> iterator = series.listIterator(maxSize);
      Series<X, Y> others = iterator.next();
      String seriesName = "others (" + (series.size() - maxSize) + ")";
      while (iterator.hasNext()) {
        others = others.merge(seriesName, iterator.next());
      }
      result.add(others);
      return result;
    }
  }

  public static ChartBuilder create(String label, String series, String reduction) {
    String labelAttribute = series.contains(",") ? series.substring(0, series.indexOf(",")) : series;
    String dataPointAttribute = series.contains(",") ? series.substring(series.indexOf(",") + 1) : null;
    String reductionType = reduction.contains(":") ? reduction.substring(0, reduction.indexOf(":")) : reduction;
    String reductionAttribute = reduction.contains(":") ? reduction.substring(reduction.indexOf(":") + 1) : null;

    return switch (reductionType) {
      case "count" -> new ChartBuilder<Object, Object>(
        createLabelFunction(label, labelAttribute),
        createSeriesFunction(dataPointAttribute),
        createDataPointAttribute(dataPointAttribute, reductionAttribute),
        data -> data.stream().reduce(0, (left, right) -> (Integer) left + (Integer) right),
        ArrayList::new);
      case "sum" -> new ChartBuilder<Object, Object>(
        createLabelFunction(label, labelAttribute),
        createSeriesFunction(dataPointAttribute),
        createDataPointAttribute(dataPointAttribute, reductionAttribute),
        data -> data.stream().reduce(0, (left, right) -> (Double) left + (Double) right),
        ArrayList::new);
      case "unique" -> new ChartBuilder<Object, Object>(
        createLabelFunction(label, labelAttribute),
        createSeriesFunction(dataPointAttribute),
        createDataPointAttribute(dataPointAttribute, reductionAttribute),
        Collection::size,
        HashSet::new);
      default -> throw new UnbelievableException("Invalid reduction expression");
    };
  }

  private static Function createLabelFunction(String expression, String attribute) {
    Pattern pattern = Pattern.compile("^(?<type>\\w+)(\\.(?<parameter>\\w+))?$");
    Matcher matcher = pattern.matcher(expression);

    if (matcher.find()) {
      String type = matcher.group("type");

      return switch (type) {
        case "temporal" -> Labels.temporal(
          TimeUnit.valueOf(matcher.group("parameter").toUpperCase()),
          attribute
        );
        case "category" -> Labels.category(attribute);
        default -> throw new UnbelievableException("Invalid label expression");
      };
    }
    throw new UnbelievableException("Invalid label expression");
  }

  private static Function<DataEntry, String> createSeriesFunction(String attribute) {
    return attribute != null ? entry -> entry.valueOf(attribute, String.class) :
      entry -> "Series";
  }

  private static Function<DataEntry, Object> createDataPointAttribute(String dataPointAttribute, String reductionAttribute) {
    return reductionAttribute != null ? entry -> entry.valueOf(reductionAttribute) :
      dataPointAttribute != null ? entry -> entry.valueOf(dataPointAttribute) :
        entry -> 1;
  }

}
