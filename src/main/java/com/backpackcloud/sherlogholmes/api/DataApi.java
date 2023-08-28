package com.backpackcloud.sherlogholmes.api;

import com.backpackcloud.cli.preferences.UserPreferences;
import com.backpackcloud.sherlogholmes.Preferences;
import com.backpackcloud.sherlogholmes.domain.DataFilter;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.FilterFactory;
import com.backpackcloud.sherlogholmes.domain.chart.Chart;
import com.backpackcloud.sherlogholmes.domain.chart.ChartBuilder;
import com.backpackcloud.sherlogholmes.impl.WebChart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;

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
  public Response getData(@QueryParam("label") String label,
                          @QueryParam("series") String series,
                          @QueryParam("reduction") String reduction,
                          @QueryParam("filter") String filter) {
    ChartBuilder builder = ChartBuilder.create(label, series, reduction);
    DataFilter dataFilter = Arrays.stream(filter != null ? filter.split(",") : new String[0])
      .map(filterFactory::create)
      .reduce(DataFilter.ALLOW_ALL, DataFilter::and);

    Chart chart = builder.build(registry.entries().stream().filter(dataFilter));
    WebChart webChart = new WebChart(
      "chart",
      chart,
      preferences.number(Preferences.MAX_SERIES_COUNT).get()
    );

    return Response.ok(webChart).build();
  }

}
