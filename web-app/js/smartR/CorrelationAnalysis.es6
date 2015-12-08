function buildCorrelationAnalysis(results) {
    const animationDuration = 500
    const panel = $('#etrikspanel')
    const margin = {top: 20, right: 40, bottom: 40, left: 10}
    const width = panel.width() * 2 / 3 - 10 - margin.left - margin.right
    const height = panel.height() * 2 / 3 - 10 - margin.top - margin.bottom
    const colors = ['#33FF33', '#3399FF', '#CC9900', '#CC99FF', '#FFFF00', 'blue']
    const x = d3.scale.linear().range([0, width])
    const y = d3.scale.linear().range([height, 0])
    let correlation, pvalue, regLineSlope, regLineYIntercept, xArr, yArr, patientIDs, tags, points, xArrLabel, yArrLabel, method

    function setData(data) {
        correlation = data.correlation[0]
        pvalue = data.pvalue[0]
        regLineSlope = data.regLineSlope[0]
        regLineYIntercept = data.regLineYIntercept[0]
        xArrLabel = data.xArrLabel[0]
        yArrLabel = data.yArrLabel[0]
        method = data.method[0]
        patientIDs = data.patientIDs
        tags = data.tags
        points = data.points
        x.domain(d3.extent(points, d => d.x))
        y.domain(d3.extent(points, d => d.y))
    }

    setData(results)
    const X = x.copy()
    const Y = y.copy()

    function updateStatistics(patientIDs, scatterUpdate=false) {
        const settings = {patientIDs}
        const onResponse = response => {
            setData(response)
            updateRegressionLine()
            updateLegend()
            if (scatterUpdate) updateScatterplot()
        }
        startWorkflow(onResponse, settings, false, false)
    }

    const svg = d3.select('#scatterplot').append('svg')
        .attr('width', width + margin.left + margin.right)
        .attr('height', height + margin.top + margin.bottom)
        .append('g')
        .attr('transform', `translate(${margin.left}, ${margin.top})`)
        .on('contextmenu', () => {
            d3.event.preventDefault()
            contextMenu
                .style('visibility', 'visible')
                .style('left', mouseX() + 'px')
                .style('top', mouseY() + 'px')
        })

    const tooltip = d3.select('#scatterplot').append('div')
        .attr('class', 'tooltip')
        .style('visibility', 'hidden')

    function dragmove() {
        legend
            .style('left', mouseX() + 'px')
            .style('top', mouseY() + 'px')
    }

    const drag = d3.behavior.drag()
        .on('drag', dragmove)

    const scatterPos = $('#scatterplot').position()
    const legend = d3.select('#scatterplot').append('div')
        .attr('class', 'legend')
        .style('left', scatterPos.left + margin.left + 'px')
        .style('top', scatterPos.top + margin.top + 'px')
        .call(drag)

    svg.append('g')
        .attr('class', 'x axis')
        .attr('transform', 'translate(0, 0)')
        .call(d3.svg.axis()
            .scale(x)
            .ticks(10)
            .tickFormat('')
            .innerTickSize(height)
            .orient('bottom'))

    svg.append('text')
        .attr('class', 'axisLabels')
        .attr('x', width / 2)
        .attr('y', -margin.top + 15)
        .text(shortenConcept(xArrLabel))

    svg.append('g')
        .attr('class', 'y axis')
        .attr('transform', `translate(${width}, ${0})`)
        .call(d3.svg.axis()
            .scale(y)
            .ticks(10)
            .tickFormat('')
            .innerTickSize(width)
            .orient('left'))

    svg.append('text')
        .attr('class', 'axisLabels')
        .attr('x', height / 2)
        .attr('y', -width - margin.right + 30)
        .attr('transform', 'rotate(90)')
        .text(shortenConcept(yArrLabel))

    svg.append('g')
        .attr('class', 'x axis')
        .attr('transform', `translate(${0}, ${height})`)
        .call(d3.svg.axis()
            .scale(x)
            .orient('top'))

    svg.append('g')
        .attr('class', 'y axis')
        .attr('transform', `translate(${0}, ${0})`)
        .call(d3.svg.axis()
            .scale(y)
            .orient('right'))

    let bins = 10
    d3.select('#binNumber')
        .attr('value', bins)
        .on('change', () => {})

    function updateBinNumber() {
        bins = parseInt($('#binNumber').val())
    }

    function updateCohorts() {
        alert('This feature will be available in TranSMART 1.3')
    }

    function excludeSelection() {
        const remainingPatientIDs = d3.selectAll('.point:not(.selected)').map(d => d.patientID)
        updateStatistics(remainingPatientIDs, true)
    }

    function zoomSelection() {
        if (d3.selectAll('.point.selected').size() < 2) {
            alert('Please select at least two elements before zooming!')
            return
        }
        var selectedPatientIDs = d3.selectAll('.point.selected').data(d => d.patientID)
        updateStatistics(selectedPatientIDs)
    }

    const ctxHtml = 'Number of bins<br/> \
            <input id="binNumber" class="mybutton" type="number" min="1" max="20" step="1"/><br/> \
            <input id="updateCohortsButton" class="mybutton" type="button" value="Update Cohorts"/><br/> \
            <input id="zoomButton" class="mybutton" type="button" value="Zoom"/><br/> \
            <input id="excludeButton" class="mybutton" type="button" value="Exclude"/><br/> \
            <input id="resetButton" class="mybutton" type="button" value="Reset"/>'
    const contextMenu = d3.select('#scatterplot').append('div')
        .attr('class', 'contextMenu')
        .style('visibility', 'hidden')
        .html(ctxHtml)
    $('#binNumber').on('click', updateBinNumber)
    $('#updateCohortsButton').on('click', updateCohorts)
    $('#zoomButton').on('click', zoomSelection)
    $('#excludeButton').on('click', excludeSelection)
    $('#resetButton').on('click', reset)

    function updateSelection() {
        let extent = brush.extent()
        let x0 = X.invert(extent[0][0]),
            y1 = Y.invert(extent[0][1]),
            x1 = X.invert(extent[1][0]),
            y0 = Y.invert(extent[1][1])
        svg.selectAll('.point')
            .classed('selected', false)
            .style('fill', d => getColor(d))
            .style('stroke', 'white')
            .filter(d => {
                return x0 <= d.x && d.x <= x1 && y0 <= d.y && d.y <= y1
            })
            .classed('selected', true)
            .style('fill', 'white')
            .style('stroke', d => getColor(d))
    }

    const brush = d3.svg.brush()
        .x(d3.scale.identity().domain([0, width]))
        .y(d3.scale.identity().domain([0, height]))
        .on('brushend', () => {
            contextMenu
                .style('visibility', 'hidden')
                .style('top', -100 + 'px')
            updateSelection()
            const selectedPatientIDs = d3.selectAll('.point.selected').data().map(d => d.patientID)
            updateStatistics(selectedPatientIDs)
        })

    svg.append('g')
        .attr('class', 'brush')
        .on('mousedown', () => d3.event.button === 2 ? d3.event.stopImmediatePropagation() : null)
        .call(brush)

    function getColor(point) {
        return point.tag ? colors[tags.indexOf(tag)] : 'black'
    }

    function updateScatterplot() {
        let point = svg.selectAll('.point')
            .data(points, d => d.patientID)

        point.enter()
            .append('circle')
            .attr('class', 'point')
            .attr('cx', d => x(d.x))
            .attr('cy', d => y(d.y))
            .attr('r', 5)
            .style('fill', d => getColor(d))
            .on('mouseover', function (d) {
                d3.select(this).style('fill', '#FF0000')
                tooltip
                    .style('left', 10 + mouseX() + 'px')
                    .style('top', 10 + mouseY() + 'px')
                    .style('visibility', 'visible')
                    .html(`${shortenConcept(xArrLabel)}: ${d.x}<br/>
                            ${shortenConcept(yArrLabel)}: ${d.y}<br/>
                            Patient ID: ${d.patientID}<br/>
                            ${d.tag ? 'Tag: ' + d.tag : ''}`)
            })
            .on('mouseout', function () {
                const p = d3.select(this)
                if (p.classed('selected')) {
                    p.style('fill', '#FFFFFF')
                }
                else {
                    p.style('fill', d => getColor(d))
                }
                tooltip.style('visibility', 'hidden')
            })

        point.exit()
            .transition()
            .duration(animationDuration)
            .attr('r', 0)
            .remove()
    }

    function updateLegend() {
        let html = (`Correlation Coefficient: ${correlation}<br/>
                    p-value: ${pvalue}<br/>
                    Method: ${method}<br/><br/>
                    Selected: ${d3.selectAll('.point.selected').size()}<br/>
                    Displayed: ${d3.selectAll('.point').size()}<br/><br/>`)
        html = html + `<p style='background:#000000; color:#FFFFFF'>Default</p>`
        for (let tag of tags) {
            html = html + `<p style='background:${getColor(tag)}; color:#000000'>${tag}</p>`
        }
        legend.html(html)
    }

    function updateRegressionLine() {
        const searchSpace = d3.selectAll('.point.selected').empty() ? d3.selectAll('.point') : d3.selectAll('.point.selected')
        const [minX, maxX] = d3.extent(searchSpace.data().map(d => d.x))
        const regressionLine = svg.selectAll('.regressionLine')
            .data([1], d => d)

        regressionLine.enter()
            .append('line')
            .attr('class', 'regressionLine')
            .on('mouseover', function () {
                d3.select(this).attr('stroke', 'red')
                tooltip
                    .style('visibility', 'visible')
                    .html(`slope: ${regLineSlope}<br/>intercept: ${regLineYIntercept}`)
                    .style('left', mouseX() + 'px')
                    .style('top', mouseY() + 'px')
            })
            .on('mouseout', function () {
                d3.select(this).attr('stroke', 'orange')
                tooltip.style('visibility', 'hidden')
            })

        regressionLine
            .transition()
            .duration(animationDuration)
            .attr('x1', X(minX))
            .attr('y1', Y(regLineYIntercept + regLineSlope * minX))
            .attr('x2', X(maxX))
            .attr('y2', Y(regLineYIntercept + regLineSlope * maxX))
    }

    function reset() {
        updateStatistics([], true)
    }


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
    */

    updateScatterplot()
    updateRegressionLine()
    updateLegend()
}