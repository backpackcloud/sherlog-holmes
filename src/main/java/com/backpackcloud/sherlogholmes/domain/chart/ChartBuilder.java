package com.backpackcloud.sherlogholmes.domain.chart;

import com.backpackcloud.sherlogholmes.domain.DataEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ChartBuilder<X, Y> {

  private final List<Label<X>> labels;
  private final Function<DataEntry, String> seriesFunction;
  private final Function<DataEntry, Y> dataPointFunction;
  private final Function<Collection<Y>, Y> reducerFunction;
  private final Supplier<Collection<Y>>  collectionSupplier;

  public ChartBuilder(List<Label<X>> labels,
                      Function<DataEntry, String> seriesFunction,
                      Function<DataEntry, Y> dataPointFunction,
                      Function<Collection<Y>, Y> reducerFunction,
                      Supplier<Collection<Y>> collectionSupplier) {
    this.labels = labels;
    this.seriesFunction = seriesFunction;
    this.dataPointFunction = dataPointFunction;
    this.reducerFunction = reducerFunction;
    this.collectionSupplier = collectionSupplier;
  }

  public Chart<X, Y> build(List<DataEntry> data) {
    Map<String, Map<Label<X>, Collection<Y>>> seriesData = new HashMap<>();

    Iterator<Label<X>> iterator = labels.iterator();
    Label<X> currentLabel = iterator.next();
    for (DataEntry entry : data) {
      while (!currentLabel.includes(entry)) {
        currentLabel = iterator.next();
      }
      String series = seriesFunction.apply(entry);
      if (!seriesData.containsKey(series)) {
        seriesData.put(series, new HashMap<>());
      }
      Map<Label<X>, Collection<Y>> labelSeries = seriesData.get(series);
      if (!labelSeries.containsKey(currentLabel)) {
        labelSeries.put(currentLabel, collectionSupplier.get());
      }
      labelSeries.get(currentLabel).add(dataPointFunction.apply(entry));
    }

    Map<String, Series<X, Y>> seriesMap = new HashMap<>();

    seriesData.forEach((seriesName, rawData) -> {
      List<DataPoint<X, Y>> dataPoints = new ArrayList<>();
      rawData.forEach((label, values) -> dataPoints.add(new DataPointImpl<>(label, reducerFunction.apply(values))));
      seriesMap.put(seriesName, new SeriesImpl<>(seriesName, labels, dataPoints, reducerFunction));
    });

    return new ChartImpl<>(labels, new ArrayList<>(seriesMap.values()));
  }

  private record SeriesImpl<X, Y>(String name,
                               List<Label<X>> labels,
                               List<DataPoint<X, Y>> data,
                               Function<Collection<Y>, Y> reducerFunction) implements Series<X, Y> {

    @Override
    public Series<X, Y> merge(String newName, Series<X, Y> other) {
      List<DataPoint<X, Y>> newData = new ArrayList<>();

      Iterator<DataPoint<X, Y>> lefIterator = this.data.iterator();
      Iterator<DataPoint<X, Y>> rightIterator = other.data().iterator();

      // assuming both series have the same labels
      while (lefIterator.hasNext()) {
        DataPoint<X, Y> left = lefIterator.next();
        DataPoint<X, Y> right = rightIterator.next();
        newData.add(new DataPointImpl<>(left.label(), reducerFunction.apply(List.of(left.value(), right.value()))));
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

}
