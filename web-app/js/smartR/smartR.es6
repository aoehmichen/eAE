function createD3Button(args) {
    let button = args.location.append('g')

    let box = button.append('rect')
        .attr('x', args.x)
        .attr('y', args.y)
        .attr('rx', 3)
        .attr('ry', 3)
        .attr('width', args.width)
        .attr('height', args.height)
        .style('stroke-width', '1px')
        .style('stroke', '#009ac9')
        .style('fill', '#009ac9')
        .style('cursor', 'pointer')
        .on('mouseover', () => {
            box
                .transition()
                .duration(300)
                .style('fill', '#ffffff')

            text
                .transition()
                .duration(300)
                .style('fill', '#009ac9')
        })
        .on('mouseout', () => {
            box
                .transition()
                .duration(300)
                .style('fill', '#009ac9')

            text
                .transition()
                .duration(300)
                .style('fill', '#ffffff')
        })
        .on('click', () => args.callback())

    let text = button.append('text')
        .attr('x', args.x + args.width / 2)
        .attr('y', args.y + args.height / 2)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#ffffff')
        .style('font-size', '14px')
        .text(args.label)

    return button
}

function createD3Switch(args) {
    let switcher = args.location.append('g')

    let checked = args.checked
    let color = checked ? 'green' : 'red'

    let box = switcher.append('rect')
        .attr('x', args.x)
        .attr('y', args.y)
        .attr('rx', 3)
        .attr('ry', 3)
        .attr('width', args.width)
        .attr('height', args.height)
        .style('stroke-width', '1px')
        .style('stroke', color)
        .style('fill', color)
        .style('cursor', 'pointer')
        .on('click', () => {
            if (color === 'green') {
                box
                    .transition()
                    .duration(300)
                    .style('stroke', 'red')
                    .style('fill', 'red')
                color = 'red'
                checked = false
            } else {
                box
                    .transition()
                    .duration(300)
                    .style('stroke', 'green')
                    .style('fill', 'green')
                color = 'green'
                checked = true
            }
            text.text(checked ? args.onlabel : args.offlabel)
            args.callback(checked)
        })

    let text = switcher.append('text')
        .attr('x', args.x + args.width / 2)
        .attr('y', args.y + args.height / 2)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#ffffff')
        .style('font-size', '14px')
        .text(checked ? args.onlabel : args.offlabel)

    return switcher
}

function createD3Dropdown(args) {
    function shrink() {
        dropdown.selectAll('.itemBox')
            .attr('y', args.y + args.height)
            .style('visibility', 'hidden')
        dropdown.selectAll('.itemText')
            .attr('y', args.y + args.height + args.height / 2)
            .style('visibility', 'hidden')
        itemHovered = false
        hovered = false
        itemHovered = false
    }
    let dropdown = args.location.append('g')

    let hovered = false
    let itemHovered = false

    let itemBox = dropdown.selectAll('.itemBox')
        .data(args.items, item => item.label)

    itemBox
        .enter()
        .append('rect')
        .attr('class', 'itemBox')
        .attr('x', args.x)
        .attr('y', args.y + args.height)
        .attr('rx', 0)
        .attr('ry', 0)
        .attr('width', args.width)
        .attr('height', args.height)
        .style('cursor', 'pointer')
        .style('stroke-width', '2px')
        .style('stroke', '#ffffff')
        .style('fill', '#E3E3E3')
        .style('visibility', 'hidden')
        .on('mouseover', () => {
            itemHovered = true
            d3.select(this)
                .style('fill', '#009ac9')
        })
        .on('mouseout', () => {
            itemHovered = false
            d3.select(this)
                .style('fill', '#E3E3E3')
            setTimeout(() => {
                if (! hovered && ! itemHovered) {
                    shrink()
                }
            }, 50)
        })
        .on('click', d => d.callback())

    let itemText = dropdown.selectAll('.itemText')
        .data(args.items, item => item.label)

    itemText
        .enter()
        .append('text')
        .attr('class', 'itemText')
        .attr('x', args.x + args.width / 2)
        .attr('y', args.y + args.height + args.height / 2)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#000000')
        .style('font-size', '14px')
        .style('visibility', 'hidden')
        .text(d => d.label)

    let box = dropdown.append('rect')
        .attr('x', args.x)
        .attr('y', args.y)
        .attr('rx', 3)
        .attr('ry', 3)
        .attr('width', args.width)
        .attr('height', args.height)
        .style('stroke-width', '1px')
        .style('stroke', '#009ac9')
        .style('fill', '#009ac9')
        .on('mouseover', () => {
            if (hovered) {
                return
            }
            dropdown.selectAll('.itemBox')
                .transition()
                .duration(300)
                .style('visibility', 'visible')
                .attr('y', d => {
                    let idx = args.items.findIndex(item => item.label === d.label)
                    return 2 + args.y + (idx + 1) * args.height
                })

            dropdown.selectAll('.itemText')
                .transition()
                .duration(300)
                .style('visibility', 'visible')
                .attr('y', function(d) {
                    let idx = args.items.findIndex(item => item.label === d.label)
                    return 2 + args.y + (idx + 1) * args.height + args.height / 2
                })

            hovered = true
        })
        .on('mouseout', () => {
            hovered = false
            setTimeout(() => {
                if (! hovered && ! itemHovered) {
                    shrink()
                }
            }, 50)
            setTimeout(() => { // first check is not enough if animation interrupts it
                if (! hovered && ! itemHovered) {
                    shrink()
                }
            }, 350)
        })

    let text = dropdown.append('text')
        .attr('class', 'buttonText')
        .attr('x', args.x + args.width / 2)
        .attr('y', args.y + args.height / 2)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#ffffff')
        .style('font-size', '14px')
        .text(args.label)

    return dropdown
}

function createD3Slider(args) {
    let slider = args.location.append('g')

    let lineGen = d3.svg.line()
        .x(d => d.x)
        .y(d => d.y)
        .interpolate('linear')

    let lineData = [
        {x: args.x, y: args.y + args.height},
        {x: args.x, y: args.y + 0.75 * args.height},
        {x: args.x + args.width, y: args.y + 0.75 * args.height},
        {x: args.x + args.width, y: args.y + args.height}
    ]

    let sliderScale = d3.scale.linear()
        .domain([args.min, args.max])
        .range([args.x, args.x + args.width])

    slider.append('path')
        .attr('d', lineGen(lineData))
        .style('pointer-events', 'none')
        .style('stroke', '#009ac9')
        .style('stroke-width', '2px')
        .style('shape-rendering', 'crispEdges')
        .style('fill', 'none')

    slider.append('text')
        .attr('x', args.x)
        .attr('y', args.y + args.height + 10)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#000000')
        .style('font-size', '9px')
        .text(args.min)

    slider.append('text')
        .attr('x', args.x + args.width)
        .attr('y', args.y + args.height + 10)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#000000')
        .style('font-size', '9px')
        .text(args.max)

    slider.append('text')
        .attr('x', args.x + args.width / 2)
        .attr('y', args.y + args.height)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#000000')
        .style('font-size', '14px')
        .text(args.label)

    let currentValue = args.init

    function move() {
        let xPos = d3.event.x
        if (xPos < args.x) {
            xPos = args.x
        } else if (xPos > args.x + args.width) {
            xPos = args.x + args.width
        }

        currentValue = Number(sliderScale.invert(xPos)).toFixed(5)

        dragger
            .attr('x', xPos - 20)
        handle
            .attr('cx', xPos)
        pointer
            .attr('x1', xPos)
            .attr('x2', xPos)
        value
            .attr('x', xPos + 10)
            .text(currentValue)
    }

    let drag = d3.behavior.drag()
        .on('drag', move)
        .on(args.trigger, () => { args.callback(currentValue) })

    let dragger = slider.append('rect')
        .attr('x', sliderScale(args.init) - 20)
        .attr('y', args.y)
        .attr('width', 40)
        .attr('height', args.height)
        .style('opacity', 0)
        .style('cursor', 'pointer')
        .on('mouseover', () => {
            handle
                .style('fill', '#009ac9')
            pointer
                .style('stroke', '#009ac9')
        })
        .on('mouseout', () => {
            handle
                .style('fill', '#000000')
            pointer
                .style('stroke', '#000000')
        })
        .call(drag)

    let handle = slider.append('circle')
        .attr('cx', sliderScale(args.init))
        .attr('cy', args.y + 10)
        .attr('r', 6)
        .style('pointer-events', 'none')
        .style('fill', '#000000')

    let pointer = slider.append('line')
        .attr('x1', sliderScale(args.init))
        .attr('y1', args.y + 10)
        .attr('x2', sliderScale(args.init))
        .attr('y2', args.y + 0.75 * args.height)
        .style('pointer-events', 'none')
        .style('stroke', '#000000')
        .style('stroke-width', '1px')

    let value = slider.append('text')
        .attr('x', sliderScale(args.init) + 10)
        .attr('y', args.y + 10)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'start')
        .style('fill', '#000000')
        .style('font-size', '10px')
        .text(args.init)

    return slider
}

function mouseX() {
    let mouseXPos = typeof d3.event.sourceEvent !== 'undefined' ? d3.event.sourceEvent.pageX : d3.event.clientX
    return mouseXPos - $('#etrikspanel').offset().left + $('#index').parent().scrollLeft()
}

function mouseY() {
    let mouseYPos = typeof d3.event.sourceEvent !== 'undefined' ? d3.event.sourceEvent.pageY : d3.event.clientY
    return mouseYPos + $('#index').parent().scrollTop() - $('#etrikspanel').offset().top
}

function showCohortInfo() {
    let cohortsSummary = ''

    for(let i of Array(GLOBAL.NumOfSubsets).keys()) {
        let currentQuery = getQuerySummary(i + 1)
        if(currentQuery !== '') {
            cohortsSummary += '<br/>Subset ' + (i + 1) + ': <br/>'
            cohortsSummary += currentQuery
            cohortsSummary += '<br/>'
        }
    }
    if (!cohortsSummary) {
        cohortsSummary = '<br/>WARNING: No subsets have been selected! Please go to the "Comparison" tab and select your subsets.'
    }
    $('#cohortInfo').html(cohortsSummary)
}
showCohortInfo()

function updateInputView() {
    if (typeof updateOnView === 'function') {
        updateOnView()
    }
}

let panelItem = $('#resultsTabPanel__etrikspanel')
panelItem.click(showCohortInfo)
panelItem.click(updateInputView)

function getMaxWidth(arr) {
    return Math.max.apply(null, arr.map(() => this.getBBox().width))
}

function shortenConcept(concept) {
    let splits = concept.split('\\')
    return splits[splits.length - 3] + '/' + splits[splits.length - 2]
}

function activateDragAndDrop(divName) {
    let div = Ext.get(divName)
    let dtgI = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'})
    dtgI.notifyDrop = dropOntoCategorySelection
}

function clearVarSelection(divName) {
    $('#' + divName).remove()
}

function getConcepts(divName) {
    return $('#' + divName).children().toArray().map(childNode => childNode.getAttribute('conceptid'))
}

let conceptBoxes = []
let sanityCheckErrors = []
function registerConceptBox(name, cohorts, type='valueicon', min=0, max=Number.MAX_SAFE_INTEGER) {
    const concepts = getConcepts(name)
    const check1 = containsOnly(name, type)
    const check2 = concepts.length >= min
    const check3 = concepts.length <= max
    sanityCheckErrors.push(
        !check1 ? `Concept box (${name}) contains concepts with invalid type! Valid type: ${type}` :
            !check2 ? `Concept box (${name}) contains too few concepts! Valid range: ${min} - ${max}` :
                !check3 ? `Concept box (${name}) contains too many concepts! Valid range: ${min} - ${max}` : '')
    conceptBoxes.push({name, cohorts, type, concepts})
}

function containsOnly(divName, icon) {
    // FIXME: the part after || is only because alphaicon does not exist in the current master branch
    return $('#' + divName).children().toArray().every(childNode => childNode.getAttribute('setnodetype') === icon || icon === 'alphaicon')
}

function sane() { // FIXME: somehow check for subset2 to be non empty iff two cohorts are needed
    if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
        alert('No cohorts have been selected. Please drag&drop cohorts to the fields within the "Comparison" tab')
        return false
    }

    if (!$('#scriptSelect').val()) {
        alert('Please select the algorithm you want to use!')
        return false
    }

    for (let sanityCheckError of sanityCheckErrors) {
        if (sanityCheckError) {
            alert(sanityCheckError)
            return false
        }
    }
    return customSanityCheck() // method MUST be implemented by _inFoobarAnalysis.gsp
}

function setSmartRCookie() {
    let cookies = document.cookie.split(';')
    let cookie = cookies.find(cookie => cookie.split('=')[0] === 'SmartR')
    if (cookie) return cookie.split('=')[1]
    let id = new Date().getTime() + Math.floor((Math.random() * 9999999999) + 1000000000)
    document.cookie = 'SmartR=' + id
    return id
}

function goToEAE() {
    const request = $.ajax({
        url: pageInfo.basePath + '/SmartR/goToEAEngine' ,
        type: 'POST',
        timeout: '600000'
    })
    request.done(response => $('#index').html(response))
    request.fail(() => $('#index').html('AJAX CALL FAILED!'))
}


function displayErrorMsg(params) {
    const request = $.ajax({
        url: pageInfo.basePath + '/SmartR/getMsg',
        type: "POST",
        timeout: 600000,
        data: {id: params.id}
    })
    request.done(response => {
        alert(response)
    })
    request.fail(() => alert('Server does not respond. Network connection lost?'))
}

function executeOnState(params, checkFreq) {
    const request = $.ajax({
        url: pageInfo.basePath + '/SmartR/getState',
        type: 'POST',
        timeout: 600000,
        data: {id: params.id}
    })
    request.done(response => {
        switch(response) {
            case 'ERROR':
                $('#submitButton').prop('disabled', false)
                displayErrorMsg(params)
                checkFreq = -1
                break
            case 'INIT':
                loadDataIntoSession(params)
                checkFreq = 500
                break
            case 'LOADED':
                runWorkflowScript(params)
                checkFreq = 100
                break
            case 'COMPLETE':
                renderResults(params)
                checkFreq = -1
                break
            case 'EXIT':
                checkFreq = -1
                break
        }
        if (~checkFreq) setTimeout(() => executeOnState(params), checkFreq)
    })
    request.fail(() => alert('Server does not respond. Network connection lost?'))
}

function renderResults(params) {
    $('#submitButton').prop('disabled', false);
    const request = $.ajax({
        url: pageInfo.basePath + '/SmartR/renderResults',
        type: 'POST',
        timeout: 600000,
        data: {id: params.id,
            script: params.script,
            redraw: params.redraw}
    })
    request.done(response => {
        if (params.redraw) {
            params.callback()
            $('#outputDIV').html(response)
        } else {
            params.callback(JSON.parse(response))
        }
    })
    request.fail(() => alert('Server does not respond. Network connection lost?'))
}

function runWorkflowScript(params) {
    $.ajax({
        url: pageInfo.basePath + '/SmartR/runWorkflowScript' ,
        type: 'POST',
        timeout: 600000,
        data: {id: params.id,
            script: params.script}
    })
}

function loadDataIntoSession(params) {
    $.ajax({
        url: pageInfo.basePath + '/SmartR/loadDataIntoSession',
        type: 'POST',
        timeout: 600000,
        data: {id: params.id,
            rIID1: params.rIID1,
            rIID2: params.rIID2,
            conceptBoxes: params.conceptBoxes,
            settings: params.settings,
            init: params.init}
    })
}

function initSession(params) {
    const request = $.ajax({
        url: pageInfo.basePath + '/SmartR/initSession',
        type: 'POST',
        timeout: 600000,
        data: {id: params.id,
            init: params.init}
    })
    request.done(() => executeOnState(params, -1))
    request.fail(() => alert('Server does not respond. Network connection lost?'))
}

function showLoadingScreen() {
    const request = $.ajax({
        url: pageInfo.basePath + '/SmartR/renderLoadingScreen',
        type: 'POST',
        timeout: 600000
    })
    request.done(response => $('#outputDIV').html(response))
    request.fail(() => $('#outputDIV').html('Server does not respond. Network connection lost?'))
}

function startWorkflow(visualizationCallback=()=>{}, settings=getSettings(), init=true, redraw=true) {
    settings = JSON.stringify($.extend(getSettings(), settings))
    $('#submitButton').prop('disabled', true)
    conceptBoxes = []
    sanityCheckErrors = []
    register()
    if (! sane()) return false
    if(!(isSubsetEmpty(1) || GLOBAL.CurrentSubsetIDs[1]) || !( isSubsetEmpty(2) || GLOBAL.CurrentSubsetIDs[2])) {
        runAllQueries(startWorkflow)
        return false
    }
    if (init && redraw) showLoadingScreen()

    const parameters = {
        id: setSmartRCookie(),
        settings: settings,
        init: init,
        redraw: redraw,
        callback: visualizationCallback,
        rIID1: GLOBAL.CurrentSubsetIDs[1],
        rIID2: GLOBAL.CurrentSubsetIDs[2],
        conceptBoxes: JSON.stringify(conceptBoxes),
        script: $('#scriptSelect').val()
    }

    initSession(parameters)
}

function changeInputDIV() {
    $('#outputDIV').html('')
    const request = $.ajax({
        url: pageInfo.basePath + '/SmartR/renderInputDIV',
        type: 'POST',
        timeout: 600000,
        data: {'script': $('#scriptSelect').val()}
    })
    request.done(response => {
        $('#inputDIV').html(response)
        updateInputView()
    })
    request.fail(() => $('#inputDIV').html('Server does not respond. Network connection lost?'))
}

function contact() {
    const version = 0.5

    alert(`
Before reporting a bug...
... 1. Make sure you use the lastet SmartR version (installed version: ${version})
... 2. Make sure that all requirements for using SmartR are met

All relevant information can be found on https://github.com/sherzinger/SmartR

If you still want to report a bug you MUST include these information:

>>>${navigator.userAgent} SmartR/${version}<<<

Bug reports -> http://usersupport.etriks.org/
Feedback -> sascha.herzinger@uni.lu`)
}
