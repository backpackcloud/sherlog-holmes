/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Marcelo Guimarães
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

let socket;
let chart;

$(document).ready(function () {
    chart = Highcharts.chart('chart', {
        chart: {
            zoomType: 'x',
            height: (9 / 16 * 100) + '%' // 16:9 ratio
        },
        title: {
            text: 'Chartlog Holmes'
        },
        xAxis: {
            type: 'datetime',
        },
        yAxis: {
            title: {
                text: 'Count'
            }
        },
        tooltip: {
            crosshairs: true,
            shared: true,
        },
        credits: {
            enabled: false
        },
    });

    socket = new WebSocket("ws://" + location.host + "/data");
    socket.onopen = function () {
        console.log("Connected to the web socket");
    };
    socket.onmessage = function (m) {
        let data = JSON.parse(m.data);

        while (chart.series.length > 0) {
            chart.series[0].remove(false)
        }

        data.series.forEach(function (series) {
            if (series.name === "average") {
                series.dashStyle = 'dash'
                series.type = "spline"
            } else if (series.name === "total") {
                series.type = "spline"
                series.dataLabels = {
                    enabled: true
                }
            } else {
                series.type = "column"
            }
            console.log(series)
            chart.addSeries(series, false)
        })

        chart.redraw()
    };
})