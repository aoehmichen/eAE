'use strict';

function buildCorrelationAnalysis(results) {
    var animationDuration = 500;
    var colors = ['#33FF33', '#3399FF', '#CC9900', '#CC99FF', '#FFFF00', 'blue', 'black'];
    var correlation = undefined,
        pvalue = undefined,
        regLineSlope = undefined,
        regLineYIntercept = undefined,
        xArr = undefined,
        yArr = undefined,
        patientIDs = undefined,
        tags = undefined,
        points = undefined,
        x = undefined,
        y = undefined,
        detectedTags = undefined;
    function init(data) {
        correlation = data.correlation;
        pvalue = data.pvalue;
        regLineSlope = data.regLineSlope;
        regLineYIntercept = data.regLineYIntercept;
        xArr = data.xArr;
        yArr = data.yArr;
        patientIDs = data.patientIDs;
        tags = data.tags;
        points = patientIDs.map(function (d, i) {
            return { x: xArr[i], y: yArr[i], patientID: patientIDs[i], tag: tags[i] };
        });
        x = d3.scale.linear().domain(d3.extent(points, function (d) {
            return d.x;
        })).range([0, width]);

        y = d3.scale.linear().domain(d3.extent(points, function (d) {
            return d.y;
        })).range([height, 0]);

        detectedTags = points.filter(function (d) {
            return d.tag;
        }).map(function (d) {
            return d.tag;
        }).sort();

        updateScatterplot();
        updateRegressionLine();
        updateLegend();
    }

    init(results);

    function updateStatistics(patientIDs) {
        var data = prepareFormData();
        data = addSettingsToData(data, { patientIDs: patientIDs });

        var onResponse = function onResponse(response) {
            init(response);
        };

        computeResults(onResponse, data, false, false);
    }

    var panel = $('#etrikspanel');
    var margin = { top: 20, right: 40, bottom: 40, left: 10 };
    var width = panel.width() * 2 / 3 - 10 - margin.left - margin.right;
    var height = panel.height() * 2 / 3 - 10 - margin.top - margin.bottom;

    var scatterplot = d3.select('#scatterplot').append('svg').attr('width', width + margin.left + margin.right).attr('height', height + margin.top + margin.bottom).append('g').attr('transform', 'translate(' + margin.left + ', ' + margin.top + ')').on('contextmenu', function () {
        d3.event.preventDefault();
        contextMenu.style('visibility', 'visible').style('left', mouseX() + 'px').style('top', mouseY() + 'px');
    });

    var tooltip = scatterplot.append('div').attr('class', 'tooltip').style('visibility', 'hidden');

    function dragmove() {
        legend.style('left', mouseX() + 'px').style('top', mouseY() + 'px');
    }

    var drag = d3.behavior.drag().on('drag', dragmove);

    var scatterPos = $('#scatterplot').position();
    var legend = d3.select('#scatterplot').append('div').attr('class', 'legend').style('left', scatterPos.left + margin.left + 'px').style('top', scatterPos.top + margin.top + 'px').call(drag);

    var scatterXAxis = d3.svg.axis().scale(x).ticks(10).tickFormat('').innerTickSize(height).orient('bottom');

    scatterplot.append('g').attr('class', 'x axis').attr('transform', 'translate(0, 0)').call(scatterXAxis);

    scatterplot.append('text').attr('class', 'axisLabels').attr('x', width / 2).attr('y', -margin.top + 15).text(shortenConcept(results.xArrLabel.toString()));

    var scatterYAxis = d3.svg.axis().scale(y).ticks(10).tickFormat('').innerTickSize(width).orient('left');

    scatterplot.append('g').attr('class', 'y axis').attr('transform', 'translate(' + width + ', ' + 0 + ')').call(scatterYAxis);

    scatterplot.append('text').attr('class', 'axisLabels').attr('x', height / 2).attr('y', -width - margin.right + 30).attr('transform', 'rotate(90)').text(shortenConcept(results.yArrLabel.toString()));

    var hist1Width = width / 3;
    var histogram1 = d3.select('#histogram1').append('svg').attr('width', hist1Width + margin.left + margin.right).attr('height', height + margin.top + margin.bottom).append('g').attr('transform', 'translate(' + margin.left + ', ' + margin.top + ')');

    var hist1xAxis = d3.svg.axis().scale(y).orient('right');

    var hist2Height = height / 3;
    var histogram2 = d3.select('#histogram2').append('svg').attr('width', width + margin.left + margin.right).attr('height', hist2Height + margin.top + margin.bottom).append('g').attr('transform', 'translate(' + margin.left + ', ' + margin.top + ')');

    var hist2xAxis = d3.svg.axis().scale(x).ticks(10).orient('top');

    var contextMenu = scatterplot.append('div').attr('class', 'contextMenu').style('visibility', 'hidden').html('Number of bins<br/>\n<input id=\'binNumber\' class=\'mybutton\' type=\'number\' min=\'1\' max=\'20\' step=\'1\' onchange=\'updateBinNumber()\'/><br/>\n<input id=\'updateCohortsButton\' class=\'mybutton\' type=\'button\' value=\'Update Cohorts\' onclick=\'updateCohorts()\'/><br/>\n<input id=\'zoomButton\' class=\'mybutton\' type=\'button\' value=\'Zoom\' onclick=\'zoomSelection()\'/><br/>\n<input id=\'excludeButton\' class=\'mybutton\' type=\'button\' value=\'Exclude\' onclick=\'excludeSelection()\'/><br/>\n<input id=\'resetButton\' class=\'mybutton\' type=\'button\' value=\'Reset\' onclick=\'reset()\'/>');

    var bins = 10;
    d3.select('#binNumber').attr('value', bins).on('change', updateHistogram);

    function updateBinNumber() {
        bins = parseInt($('#binNumber').val());
    }

    function updateCohorts() {
        alert('This feature will be available in TranSMART 1.3');
    }

    function updateSelection() {
        var extent = brush.extent();
        var x0 = extent[0][0],
            y0 = extent[0][1],
            x1 = extent[1][0],
            y1 = extent[1][1];
        d3.selectAll('.point').classed('selected', false).style('fill', function (d) {
            return getColor(d);
        }).style('stroke', 'white').filter(function (d) {
            return x0 <= d.x && d.x <= x1 && y0 <= d.y && d.y <= y1;
        }).classed('selected', true).style('fill', 'white').style('stroke', function (d) {
            return getColor(d);
        }).data();
    }

    function excludeSelection() {
        points = d3.selectAll('.point').filter(function (d) {
            return !d.classed('selected');
        }).data();
        updateScatterplot();
        var remainingPatientIDs = points.map(function (d) {
            return d.patientID;
        });
        updateStatistics(remainingPatientIDs);
        updateRegressionLine();
        updateLegend();
    }

    function zoomSelection() {
        if (d3.selectAll('.point.selected').length < 2) {
            alert('Please select at least two elements before zooming!');
            return;
        }
        var selectedPatientIDs = d3.selectAll('.point.selected').data(function (d) {
            return d.patientID;
        });
        updateStatistics(selectedPatientIDs);
    }

    var brush = d3.svg.brush().x(d3.scale.identity().domain([-20, width + 20])).y(d3.scale.identity().domain([-20, height + 20])).on('brushend', function () {
        contextMenu.style('visibility', 'hidden').style('top', -100 + 'px');
        updateSelection();
        var selectedPatientIDs = d3.selectAll('.point.selected').data().map(function (d) {
            return d.patientID;
        });
        updateStatistics(selectedPatientIDs);
        updateRegressionLine();
        updateLegend();
    });

    scatterplot.append('g').attr('class', 'brush').on('mousedown', function () {
        return d3.event.button === 2 ? d3.event.stopImmediatePropagation() : null;
    }).call(brush);

    function getColor(point) {
        return point.tag ? colors[detectedTags.indexOf(tag)] : 'black';
    }

    function updateScatterplot() {
        var _this = this;

        var point = d3.selectAll('.point').data(points, function (d) {
            return d.patientID;
        });

        point.enter().append('circle').attr('class', 'point').attr('cx', function (d) {
            return d.x;
        }).attr('cy', function (d) {
            return d.y;
        }).attr('r', 5).style('fill', function (d) {
            return getColor(d);
        }).on('mouseover', function (d) {
            d3.select(_this).style('fill', 'red');
            tooltip.style('left', mouseX() + 'px').style('top', mouseY() + 'px').style('visibility', 'visible').html(shortenConcept(xLabel.toString()) + ': ' + d.x + '<br/>\n' + shortenConcept(yLabel.toString()) + ': ' + d.y + '<br/>\npatientID: ' + d.patientID + '<br/>\n' + (d.tag ? 'Tag: ' + d.tag : ''));
        }).on('mouseout', function () {
            d3.select(_this);
            style('fill', function (d) {
                return getColor(d);
            });
            tooltip.style('visibility', 'hidden');
        });

        point.exit().transition().duration(animationDuration).attr('r', 0).remove();
    }

    function updateLegend() {
        //        let html = (`Correlation Coefficient: ${correlation[0]}<br/>
        //p-value: ${pvalue[0]}<br/>
        //Method: ${results.method[0]}<br/><br/>
        //Selected: ${d3.selectAll('.point.selected').length}<br/>
        //Displayed: ${d3.selectAll('.point').length}<br/><br/>`) // FIXME: add excluded to legend
        //
        //        for (let detectedTag of detectedTags) {
        //            if (!detectedTag) {
        //                html = html + `<p style='background:${getColor(detectedTag)} color:white'>Default</p>`
        //            } else {
        //                html = html + `<p style='background:${getColor(detectedTag)} color:black'>${detectedTag}</p>`
        //            }
        //        }
        //
        //        d3.select('.legend').html(html)
    }

    function updateRegressionLine() {

        //const searchSpace = d3.selectAll('.point.selected') ? d3.selectAll('.point.selected') : d3.selectAll('.point')
        //const [minX, maxX] = d3.extent(searchSpace.data().map(d => d.x))
        //
        //const regressionLine = d3.selectAll('.regressionLine')
        //    .data({regLineSlope, regLineYIntercept}, d => `slope=${d.regLineSlope} icpt=${regLineYIntercept}`)
        //
        //regressionLine.enter()
        //    .append('line')
        //    .attr('class', 'regressionLine')
        //    .transition()
        //    .duration(animationDuration)
        //    .attr('x1', minX)
        //    .attr('y1', y(parseFloat(regLineYIntercept) + parseFloat(regLineSlope) * x.invert(minX)))
        //    .attr('x2', maxX)
        //    .attr('y2', y(parseFloat(regLineYIntercept) + parseFloat(regLineSlope) * x.invert(maxX)))
        //    .on('mouseover', () => {
        //        d3.select(this).attr('stroke', 'red')
        //        tooltip
        //            .style('visibility', 'visible')
        //            .html(d => `slope: ${d.regLineSlope}<br/>intercept: ${d.regLineYIntercept}`)
        //            .style('left', mouseX() + 'px')
        //            .style('top', mouseY() + 'px')
        //    })
        //    .on('mouseout', () => {
        //        d3.select(this).attr('stroke', 'orange')
        //        tooltip.style('visibility', 'hidden')
        //    })

    }

    function reset() {}

    function updateHistogram() {
        /*
        
                let hist1Data = d3.layout.histogram()
                    .bins(bins)(d3.selectAll('point.selected').data().map(d => d.y))
        
                let hist2Data = d3.layout.histogram()
                    .bins(bins)(d3.selectAll('point.selected').data().map(d => d.x))
        
                histogram1.selectAll('*').remove()
                histogram2.selectAll('*').remove()
        
                let hist1Bar = histogram1.selectAll('.bar')
                    .data(hist1Data)
                    .enter().append('g')
                    .attr('class', 'bar')
        
                let hist2Bar = histogram2.selectAll('.bar')
                    .data(hist2Data)
                    .enter().append('g')
                    .attr('class', 'bar')
        
                let hist1BarScale = d3.scale.linear()
                    .domain([0, d3.max(hist1Data, d => d.y)])
                    .range([0, hist1Width])
        
                let hist2BarScale = d3.scale.linear()
                    .domain([0, d3.max(hist2Data, d => d.y)])
                    .range([0, hist2Height])
        
                hist1Bar.append('rect')
                    .attr('width', 0)
                    .attr('height', hist1Data[0].dx)
                    .attr('x', hist1Width)
                    .attr('y', (d, i) => hist1Data[i].x)
                    .transition()
                    .delay((d, i) => i * 25)
                    .duration(animationDuration)
                    .attr('x', d => hist1Width - hist1BarScale(d.y))
                    .attr('width', d => hist1BarScale(d.y))
        
                hist1Bar.append('text')
                    .attr('x', hist1Width)
                    .attr('y', (d, i) => hist1Data[i].x)
                    .transition()
                    .delay((d, i) => i * 25)
                    .duration(animationDuration)
                    .attr('dy', '.35em')
                    .attr('x', d => hist1Width - hist1BarScale(d.y) + 10)
                    .attr('y', (d, i) => hist1Data[i].x + hist1Data[i].dx / 2)
                    .text(d => d.y ? d.y : '')
        
                histogram1.append('g')
                    .attr('class', 'x axis')
                    .attr('transform', 'translate(' + hist1Width + ',' + 0 + ')')
                    .call(hist1xAxis)
        
                hist2Bar.append('rect')
                    .attr('width', hist2Data[0].dx)
                    .attr('height', 0)
                    .attr('x', (d, i) => hist2Data[i].x)
                    .attr('y', 0)
                    .transition()
                    .delay((d, i) => i * 25)
                    .duration(animationDuration)
                    .attr('height', d => hist2BarScale(d.y))
        
                hist2Bar.append('text')
                    .attr('x', (d, i) => hist2Data[i].x)
                    .attr('y', 0)
                    .transition()
                    .delay((d, i) => i * 25)
                    .duration(animationDuration)
                    .attr('dx', '-.5em')
                    .attr('x', (d, i) => hist2Data[i].x + hist2Data[i].dx / 2)
                    .attr('y', d => hist2BarScale(d.y) - 5)
                    .text(d => d.y ? d.y : '')
        
                histogram2.append('g')
                    .attr('class', 'x axis')
                    .attr('transform', `translate(${0}, ${0})`)
                    .call(hist2xAxis)
                    */
    }
}
