package com.backpackcloud.sherlogholmes.api;

import com.backpackcloud.cli.preferences.UserPreferences;
import com.backpackcloud.sherlogholmes.Preferences;
import com.backpackcloud.sherlogholmes.domain.AttributeType;
import com.backpackcloud.sherlogholmes.domain.DataFilter;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.FilterFactory;
import com.backpackcloud.sherlogholmes.domain.chart.Chart;
import com.backpackcloud.sherlogholmes.domain.chart.ChartBuilder;
import com.backpackcloud.sherlogholmes.impl.WebChart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/api/data")
public class DataApi {

  private final DataRegistry registry;
  private final FilterFactory filterFactory;
  private final UserPreferences preferences;

  public DataApi(DataRegistry registry, FilterFactory filterFactory, UserPreferences preferences) {
    this.registry = registry;
    this.filterFactory = filterFactory;
    this.preferences = preferences;
  }

  @GET
  @Path("/chart")
  public Response getData(@QueryParam("label") String label,
                          @QueryParam("series") String series,
                          @QueryParam("reduction") String reduction,
                          @QueryParam("filter") String filter) {
    ChartBuilder builder = ChartBuilder.create(label, series, reduction);
    DataFilter dataFilter = createFilter(filter);

    Chart chart = builder.build(registry.entries().stream().filter(dataFilter));
    WebChart webChart = new WebChart(
      "chart",
      chart,
      preferences.number(Preferences.MAX_SERIES_COUNT).get()
    );

    return Response.ok(webChart).build();
  }

  @GET
  @Path("/entries")
  public Response entries(@QueryParam("fields") String fields, String filter) {
    List<Map<String, Object>> result = new ArrayList<>();
    DataFilter dataFilter = createFilter(filter);
    String[] attributes = fields.split(",");

    registry.entries().stream()
      .filter(dataFilter)
      .forEach(entry -> {
        Map<String, Object> map = new HashMap<>();
        Arrays.stream(attributes)
          .forEach(attr -> map.put(attr, entry.valueOf(attr)));
        result.add(map);
      });

    return Response.ok(result).build();
  }

  @GET
  @Path("/index/{key}")
  public Response index(@PathParam("key") String key) {
    AttributeType type = registry.typeOf(key).orElse(AttributeType.TEXT);

    List<String> result = registry.valuesFor(key)
      .stream()
      .map(value -> type.format(value))
      .collect(Collectors.toList());

    return Response.ok(result).build();
  }

  private DataFilter createFilter(String filter) {
    DataFilter dataFilter = Arrays.stream(filter != null ? filter.split(",") : new String[0])
      .map(filterFactory::create)
      .reduce(DataFilter.ALLOW_ALL, DataFilter::and);
    return dataFilter;
  }

}
