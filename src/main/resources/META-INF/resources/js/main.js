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

let templates = [];
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
        for (let id in data) {
            templates[id] = data[id];
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
        if (m.data === "clear") {
            charts = [];
            $(".chart").remove()
            return;
        }
        let data = JSON.parse(m.data);
        let id = data.id;
        let config = data.config;
        let template = templates[config];

        if ($("#chart-" + id).length === 0) {
            $("#charts").append("<div class='chart' id='chart-" + id + "'/>")
            charts[id] = Highcharts.chart("chart-" + id, templates[config].properties)
        }

        let chart = charts[id];

        while (chart.series.length > 0) {
            chart.series[0].remove(false)
        }

        data.series.forEach(function (series) {
            let seriesProperties = template.series[series.name]
            if(typeof seriesProperties == "undefined") {
                seriesProperties = template.series["default"]
            }
            if (typeof seriesProperties != "undefined") {
                for (const prop in seriesProperties) {
                    series[prop] = seriesProperties[prop]
                }
            }
            console.log(series)
            chart.addSeries(series, false)
        })

        chart.redraw()
    };
}