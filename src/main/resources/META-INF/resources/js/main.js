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

let charts = [];

$(document).ready(function () {
    let configSocket = new WebSocket("ws://" + location.host + "/config");
    configSocket.onopen = function () {
        console.log("Connected to the config web socket");
    }
    configSocket.onmessage = function (m) {
        let data = JSON.parse(m.data);
        console.log("Received configuration")
        console.log(data)
        for (let i = 0; i < data.length; i++) {
            charts[i] = data[i];
            $("#charts").append("<div id='chart-" + i + "'/>")
            charts[i].reference = Highcharts.chart("chart-" + i, data[i].properties)
        }

        configureDataSocket()
    }
})

configureDataSocket = function () {
    let dataSocket = new WebSocket("ws://" + location.host + "/data");
    dataSocket.onopen = function () {
        console.log("Connected to the data web socket");
    };
    dataSocket.onmessage = function (m) {
        let data = JSON.parse(m.data);

        for (let i = 0; i < charts.length ; i++) {
            let chart = charts[i];

            while (chart.reference.series.length > 0) {
                chart.reference.series[0].remove(false)
            }

            data.series.forEach(function (series) {
                let seriesProperties = chart.series[series.name]
                if(typeof seriesProperties == "undefined") {
                    seriesProperties = chart.series["default"]
                }
                if (typeof seriesProperties != "undefined") {
                    for (const prop in seriesProperties) {
                        series[prop] = seriesProperties[prop]
                    }
                }
                console.log(series)
                chart.reference.addSeries(series, false)
            })

            chart.reference.redraw()
        }
    };
}