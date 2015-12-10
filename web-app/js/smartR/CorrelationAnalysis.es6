function buildCorrelationAnalysis(results) {
    const animationDuration = 500
    const bins = 10
    const panel = $('#etrikspanel')
    const margin = {top: 20, right: 20, bottom: panel.height() / 3, left: panel.width() / 3}
    const width = panel.width() * 2 / 3 - margin.left - margin.right
    const height = panel.height() * 2 / 3 - margin.top - margin.bottom
    const bottomHistHeight = margin.bottom
    const leftHistHeight = margin.left
    const colors = ['#33FF33', '#3399FF', '#CC9900', '#CC99FF', '#FFFF00', 'blue']
    const x = d3.scale.linear()
        .domain(d3.extent(results.points, d => d.x))
        .range([0, width])
    const y = d3.scale.linear()
        .domain(d3.extent(results.points, d => d.y))
        .range([height, 0])

    let correlation, pvalue, regLineSlope, regLineYIntercept, patientIDs, tags, points, xArrLabel, yArrLabel, method, minX, maxX, minY, maxY
    function setData(data) {
        correlation = data.correlation[0]
        pvalue = data.pvalue[0]
        regLineSlope = data.regLineSlope[0]
        regLineYIntercept = data.regLineYIntercept[0]
        xArrLabel = data.xArrLabel[0]
        yArrLabel = data.yArrLabel[0]
        method = data.method[0]
        patientIDs = data.patientIDs
        tags = data.tags.sort()
        points = data.points
        ;[minX, maxX] = d3.extent(data.points, d => d.x)
        ;[minY, maxY] = d3.extent(data.points, d => d.y)
    }

    setData(results)

    function updateStatistics(patientIDs, scatterUpdate=false, init=false) {
        const settings = {patientIDs}
        const onResponse = response => {
            if (init) {
                d3.selectAll('#scatterplot *').remove()
                buildCorrelationAnalysis(response)
                return
            }
            setData(response)
            if (scatterUpdate) updateScatterplot()
            updateRegressionLine()
            updateLegend()
            updateHistogram()
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
        .attr('transform', `translate(${width / 2}, ${- margin.top / 2})`)
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
        .attr('transform', `translate(${width + margin.right / 2}, ${height / 2})rotate(90)`)
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
        var selectedPatientIDs = d3.selectAll('.point.selected').map(d => d.patientID)
        updateStatistics(selectedPatientIDs, false, true)
    }

    const ctxHtml = `<input id='updateCohortsButton' class='mybutton' type='button' value='Update Cohorts'/><br/>
<input id='zoomButton' class='mybutton' type='button' value='Zoom'/><br/>
<input id='excludeButton' class='mybutton' type='button' value='Exclude'/><br/>
<input id='resetButton' class='mybutton' type='button' value='Reset'/>`
    const contextMenu = d3.select('#scatterplot').append('div')
        .attr('class', 'contextMenu')
        .style('visibility', 'hidden')
        .html(ctxHtml)
    $('#updateCohortsButton').on('click', () => { contextMenu.style('visibility', 'hidden'); updateCohorts() })
    $('#zoomButton').on('click', () => { contextMenu.style('visibility', 'hidden'); zoomSelection() })
    $('#excludeButton').on('click', () => { contextMenu.style('visibility', 'hidden'); excludeSelection() })
    $('#resetButton').on('click', () => { contextMenu.style('visibility', 'hidden'); reset() })

    function updateSelection() {
        const extent = brush.extent()
        const [x0, x1] = [extent[0][0], extent[1][0]].map(d => x.invert(d))
        const [y0, y1] = [extent[0][1], extent[1][1]].map(d => y.invert(d))
        svg.selectAll('.point')
            .classed('selected', false)
            .style('fill', d => getColor(d.tag))
            .style('stroke', 'white')
            .filter(d => x0 <= d.x && d.x <= x1 && y1 <= d.y && d.y <= y0)
            .classed('selected', true)
            .style('fill', 'white')
            .style('stroke', d => getColor(d.tag))
    }

    const brush = d3.svg.brush()
        .x(d3.scale.identity().domain([0, width]))
        .y(d3.scale.identity().domain([0, height]))
        .on('brushend', () => {
            contextMenu
                .style('visibility', 'hidden')
                .style('top', -100 + 'px')
            updateSelection()
            const selectedPatientIDs = d3.selectAll('.point.selected').map(d => d.patientID)
            updateStatistics(selectedPatientIDs)
        })

    svg.append('g')
        .attr('class', 'brush')
        .on('mousedown', () => d3.event.button === 2 ? d3.event.stopImmediatePropagation() : null)
        .call(brush)

    function getColor(tag) {
        return tag ? colors[tags.indexOf(tag)] : '#000000'
    }

    function updateScatterplot() {
        const point = svg.selectAll('.point')
            .data(points, d => d.patientID)

        point.enter()
            .append('circle')
            .attr('class', 'point')
            .attr('cx', d => x(d.x))
            .attr('cy', d => y(d.y))
            .attr('r', 5)
            .style('fill', d => getColor(d.tag))
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
                    p.style('fill', d => getColor(d.tag))
                }
                tooltip.style('visibility', 'hidden')
            })

        point.exit()
            .classed('selected', false)
            .transition()
            .duration(animationDuration)
            .attr('r', 0)
            .remove()
    }

    function updateHistogram() {
        const selX = d3.scale.linear()
            .domain([minX, maxX])
            .range([x(minX), x(maxX)])

        const bottomHistYScale = d3.scale.linear()
            .domain([0, points.size()])
            .range([0, bottomHistHeight])
        const leftHistYScale = d3.scale.linear()
            .domain([0, points.size()])
            .range([0, leftHistHeight])

        const bottomHistData = d3.layout.histogram()
            .bins(bins)(points.map(d => d.x))
            .map((d, i) => $.extend(d, {i}))
        const leftHistData = d3.layout.histogram()
            .bins(bins)(points.map(d => d.y))
            .map((d, i) => $.extend(d, {i}))

        const bottomHistBar = svg.selectAll('.bar.bottom')
            .data(bottomHistData, d => d.i)
        bottomHistBar.enter()
            .append('rect')
            .attr('class', 'bar bottom')
            .attr('y', height + 1)
        bottomHistBar.transition()
            .delay((d, i) => i * 25)
            .duration(animationDuration)
            .attr('x', d => selX(d.x))
            .attr('width', selX(bottomHistData[0].dx))
            .attr('height', d => bottomHistYScale(d.y))
        bottomHistBar.exit()
            .transition()
            .duration(animationDuration)
            .attr('height', 0)

        const leftHistBar = svg.selectAll('.bar.left')
            .data(leftHistData, d => d.i)
        leftHistBar.enter()
            .append('rect')
            .attr('class', 'bar left')
        leftHistBar.transition()
            .delay((d, i) => i * 25) 
            .duration(animationDuration)
            .attr('x', d => - leftHistYScale(d.y))
            .attr('y', (d, i) => y(minY) - (i + 1) * (y(minY) - y(maxY)) / bins)
            .attr('width', d => leftHistYScale(d.y) - 1)
            .attr('height', () => (y(minY) - y(maxY)) / bins)
        leftHistBar.exit()
            .transition()
            .duration(animationDuration)
            .attr('width', 0)

        //bottomHistBar.append('text')
        //    .attr('x', bottomHistHeight)
        //    .attr('y', (d, i) => bottomHistData[i].x)
        //    .transition()
        //    .delay((d, i) => i * 25)
        //    .duration(animationDuration)
        //    .attr('dy', '.35em')
        //    .attr('x', d => bottomHistHeight - bottomHistYScale(d.y) + 10)
        //    .attr('y', (d, i) => bottomHistData[i].x + bottomHistData[i].dx / 2)
        //    .text(d => d.y ? d.y : '')
        //
        //const leftHistBar = svg.selectAll('.bar')
        //    .data(leftHistData)
        //    .enter().append('g')
        //    .attr('class', 'bar')
        //leftHistBar.append('rect')
        //    .attr('width', leftHistData[0].dx)
        //    .attr('height', 0)
        //    .attr('x', (d, i) => leftHistData[i].x)
        //    .attr('y', 0)
        //    .transition()
        //    .delay((d, i) => i * 25)
        //    .duration(animationDuration)
        //    .attr('height', d => leftHistYScale(d.y))
        //leftHistBar.append('text')
        //    .attr('x', (d, i) => leftHistData[i].x)
        //    .attr('y', 0)
        //    .transition()
        //    .delay((d, i) => i * 25)
        //    .duration(animationDuration)
        //    .attr('dx', '-.5em')
        //    .attr('x', (d, i) => leftHistData[i].x + leftHistData[i].dx / 2)
        //    .attr('y', d => leftHistYScale(d.y) - 5)
        //    .text(d => d.y ? d.y : '')
    }

    function updateLegend() {
        let html = (`Correlation Coefficient: ${correlation}<br/>
p-value: ${pvalue}<br/>
Method: ${method}<br/><br/>
Selected: ${d3.selectAll('.point.selected').size()}<br/>
Displayed: ${d3.selectAll('.point').size()}<br/><br/>`)
        html = html + `<p style='background:#000000; color:#FFFFFF'>Default</p>`
        for (let tag of tags) {
            if (tag) html += `<p style='background:${getColor(tag)}; color:#FFFFFF'>${tag}</p>`
        }
        legend.html(html)
    }

    function updateRegressionLine() {
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

        regressionLine.transition()
            .duration(animationDuration)
            .attr('x1', x(minX))
            .attr('y1', y(regLineYIntercept + regLineSlope * minX))
            .attr('x2', x(maxX))
            .attr('y2', y(regLineYIntercept + regLineSlope * maxX))
    }

    function reset() {
        updateStatistics([], false, true)
    }

    updateScatterplot()
    updateHistogram()
    updateRegressionLine()
    updateLegend()
}