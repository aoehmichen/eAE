'use strict';

var _slicedToArray = (function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; })();

function buildCorrelationAnalysis(results) {
    var animationDuration = 500;
    var panel = $('#etrikspanel');
    var margin = { top: 20, right: 40, bottom: 40, left: 10 };
    var width = panel.width() * 2 / 3 - 10 - margin.left - margin.right;
    var height = panel.height() * 2 / 3 - 10 - margin.top - margin.bottom;
    var colors = ['#33FF33', '#3399FF', '#CC9900', '#CC99FF', '#FFFF00', 'blue'];
    var x = d3.scale.linear().range([0, width]);
    var y = d3.scale.linear().range([height, 0]);
    var correlation = undefined,
        pvalue = undefined,
        regLineSlope = undefined,
        regLineYIntercept = undefined,
        xArr = undefined,
        yArr = undefined,
        patientIDs = undefined,
        tags = undefined,
        points = undefined,
        detectedTags = undefined,
        xArrLabel = undefined,
        yArrLabel = undefined,
        method = undefined;
    function setData(data) {
        correlation = data.correlation[0];
        pvalue = data.pvalue[0];
        regLineSlope = data.regLineSlope[0];
        regLineYIntercept = data.regLineYIntercept[0];
        xArrLabel = data.xArrLabel[0];
        yArrLabel = data.yArrLabel[0];
        method = data.method[0];
        xArr = data.xArr;
        yArr = data.yArr;
        patientIDs = data.patientIDs;
        tags = data.tags;
        points = patientIDs.map(function (d, i) {
            return { x: xArr[i], y: yArr[i], patientID: patientIDs[i], tag: tags[i] };
        });
        x.domain(d3.extent(points, function (d) {
            return d.x;
        }));
        y.domain(d3.extent(points, function (d) {
            return d.y;
        }));
        detectedTags = points.filter(function (d) {
            return d.tag;
        }).map(function (d) {
            return d.tag;
        }).sort();
    }

    setData(results);

    function updateStatistics(patientIDs) {
        var settings = { patientIDs: patientIDs };
        var onResponse = function onResponse(response) {
            setData(response);
            updateRegressionLine();
            updateLegend();
        };
        startWorkflow(onResponse, settings, false, false);
    }

    var svg = d3.select('#scatterplot').append('svg').attr('width', width + margin.left + margin.right).attr('height', height + margin.top + margin.bottom).append('g').attr('transform', 'translate(' + margin.left + ', ' + margin.top + ')').on('contextmenu', function () {
        d3.event.preventDefault();
        contextMenu.style('visibility', 'visible').style('left', mouseX() + 'px').style('top', mouseY() + 'px');
    });

    var tooltip = d3.select('#scatterplot').append('div').attr('class', 'tooltip').style('visibility', 'hidden');

    function dragmove() {
        legend.style('left', mouseX() + 'px').style('top', mouseY() + 'px');
    }

    var drag = d3.behavior.drag().on('drag', dragmove);

    var scatterPos = $('#scatterplot').position();
    var legend = d3.select('#scatterplot').append('div').attr('class', 'legend').style('left', scatterPos.left + margin.left + 'px').style('top', scatterPos.top + margin.top + 'px').call(drag);

    var scatterXAxis = d3.svg.axis().scale(x).ticks(10).tickFormat('').innerTickSize(height).orient('bottom');

    svg.append('g').attr('class', 'x axis').attr('transform', 'translate(0, 0)').call(scatterXAxis);

    svg.append('text').attr('class', 'axisLabels').attr('x', width / 2).attr('y', -margin.top + 15).text(shortenConcept(xArrLabel));

    var scatterYAxis = d3.svg.axis().scale(y).ticks(10).tickFormat('').innerTickSize(width).orient('left');

    svg.append('g').attr('class', 'y axis').attr('transform', 'translate(' + width + ', ' + 0 + ')').call(scatterYAxis);

    svg.append('text').attr('class', 'axisLabels').attr('x', height / 2).attr('y', -width - margin.right + 30).attr('transform', 'rotate(90)').text(shortenConcept(yArrLabel));
    //
    //const hist1Width = width / 3
    //const histogram1 = d3.select('#histogram1').append('svg')
    //    .attr('width', hist1Width + margin.left + margin.right)
    //    .attr('height', height + margin.top + margin.bottom)
    //    .append('g')
    //    .attr('transform', `translate(${margin.left}, ${margin.top})`)
    //
    //const hist1xAxis = d3.svg.axis()
    //    .scale(y)
    //    .orient('right')
    //
    //const hist2Height = height / 3
    //const histogram2 = d3.select('#histogram2').append('svg')
    //    .attr('width', width + margin.left + margin.right)
    //    .attr('height', hist2Height + margin.top + margin.bottom)
    //    .append('g')
    //    .attr('transform', `translate(${margin.left}, ${margin.top})`)
    //
    //const hist2xAxis = d3.svg.axis()
    //    .scale(x)
    //    .ticks(10)
    //    .orient('top')

    var contextMenu = d3.select('#scatterplot').append('div').attr('class', 'contextMenu').style('visibility', 'hidden').html('Number of bins<br/>\n<input id=\'binNumber\' class=\'mybutton\' type=\'number\' min=\'1\' max=\'20\' step=\'1\' onchange=\'updateBinNumber()\'/><br/>\n<input id=\'updateCohortsButton\' class=\'mybutton\' type=\'button\' value=\'Update Cohorts\' onclick=\'updateCohorts()\'/><br/>\n<input id=\'zoomButton\' class=\'mybutton\' type=\'button\' value=\'Zoom\' onclick=\'zoomSelection()\'/><br/>\n<input id=\'excludeButton\' class=\'mybutton\' type=\'button\' value=\'Exclude\' onclick=\'excludeSelection()\'/><br/>\n<input id=\'resetButton\' class=\'mybutton\' type=\'button\' value=\'Reset\' onclick=\'reset()\'/>');

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
            return x0 <= x(d.x) && x(d.x) <= x1 && y0 <= y(d.y) && y(d.y) <= y1;
        }).classed('selected', true).style('fill', 'white').style('stroke', function (d) {
            return getColor(d);
        });
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
    }

    function zoomSelection() {
        if (d3.selectAll('.point.selected').size() < 2) {
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
    });

    svg.append('g').attr('class', 'brush').on('mousedown', function () {
        return d3.event.button === 2 ? d3.event.stopImmediatePropagation() : null;
    }).call(brush);

    function getColor(point) {
        return point.tag ? colors[detectedTags.indexOf(tag)] : 'black';
    }

    function updateScatterplot() {
        var point = svg.selectAll('.point').data(points, function (d) {
            return d.patientID;
        });

        point.enter().append('circle').attr('class', 'point').attr('cx', function (d) {
            return x(d.x);
        }).attr('cy', function (d) {
            return y(d.y);
        }).attr('r', 5).style('fill', function (d) {
            return getColor(d);
        }).on('mouseover', function (d) {
            d3.select(this).style('fill', '#FF0000');
            tooltip.style('left', 10 + mouseX() + 'px').style('top', 10 + mouseY() + 'px').style('visibility', 'visible').html(shortenConcept(xArrLabel) + ': ' + d.x + '<br/>' + shortenConcept(yArrLabel) + ': ' + d.y + '<br/>Patient ID: ' + d.patientID + '<br/>' + (d.tag ? 'Tag: ' + d.tag : ''));
        }).on('mouseout', function () {
            d3.select(this).style('fill', function (d) {
                return getColor(d);
            });
            tooltip.style('visibility', 'hidden');
        });

        point.exit().transition().duration(animationDuration).attr('r', 0).remove();
    }

    function updateLegend() {
        var html = 'Correlation Coefficient: ' + correlation + '<br/>p-value: ' + pvalue + '<br/>Method: ' + method + '<br/><br/>Selected: ' + d3.selectAll('.point.selected').size() + '<br/>Displayed: ' + d3.selectAll('.point').size() + '<br/><br/>';
        html = html + '<p style=\'background:#000000; color:#FFFFFF\'>Default</p>';
        var _iteratorNormalCompletion = true;
        var _didIteratorError = false;
        var _iteratorError = undefined;

        try {
            for (var _iterator = detectedTags[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
                var detectedTag = _step.value;

                html = html + ('<p style=\'background:' + getColor(detectedTag) + '; color:#000000\'>' + detectedTag + '</p>');
            }
        } catch (err) {
            _didIteratorError = true;
            _iteratorError = err;
        } finally {
            try {
                if (!_iteratorNormalCompletion && _iterator.return) {
                    _iterator.return();
                }
            } finally {
                if (_didIteratorError) {
                    throw _iteratorError;
                }
            }
        }

        legend.html(html);
    }

    function updateRegressionLine() {
        var searchSpace = d3.selectAll('.point.selected').empty() ? d3.selectAll('.point') : d3.selectAll('.point.selected');

        var _d3$extent = d3.extent(searchSpace.data().map(function (d) {
            return d.x;
        }));

        var _d3$extent2 = _slicedToArray(_d3$extent, 2);

        var minX = _d3$extent2[0];
        var maxX = _d3$extent2[1];

        var regressionLine = svg.selectAll('.regressionLine').data([1], function (d) {
            return d;
        });

        regressionLine.enter().append('line').attr('class', 'regressionLine').on('mouseover', function () {
            d3.select(this).attr('stroke', 'red');
            tooltip.style('visibility', 'visible').html('slope: ' + regLineSlope + '<br/>intercept: ' + regLineYIntercept).style('left', mouseX() + 'px').style('top', mouseY() + 'px');
        }).on('mouseout', function () {
            d3.select(this).attr('stroke', 'orange');
            tooltip.style('visibility', 'hidden');
        });

        regressionLine.transition().duration(animationDuration).attr('x1', x(minX)).attr('y1', y(regLineYIntercept + parseFloat(regLineSlope) * minX)).attr('x2', x(maxX)).attr('y2', y(regLineYIntercept + parseFloat(regLineSlope) * maxX));
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

    updateScatterplot();
    updateRegressionLine();
    updateLegend();
}
