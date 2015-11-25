import 'babel/polyfill'

function createD3Button(args) {
    let button = args.location.append('g')

    let box = button.append("rect")
        .attr("x", args.x)
        .attr("y", args.y)
        .attr("rx", 3)
        .attr("ry", 3)
        .attr("width", args.width)
        .attr("height", args.height)
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
        .style("text-anchor", "middle")
        .style('fill', '#ffffff')
        .style('font-size', '14px')
        .text(args.label)

    return button
}

function createD3Switch(args) {
    let switcher = args.location.append('g')

    let checked = args.checked
    let color = checked ? 'green' : 'red'

    let box = switcher.append("rect")
        .attr("x", args.x)
        .attr("y", args.y)
        .attr("rx", 3)
        .attr("ry", 3)
        .attr("width", args.width)
        .attr("height", args.height)
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
        .style("text-anchor", "middle")
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
        .attr("x", args.x)
        .attr("y", args.y + args.height)
        .attr("rx", 0)
        .attr("ry", 0)
        .attr("width", args.width)
        .attr("height", args.height)
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
        .style("text-anchor", "middle")
        .style('fill', '#000000')
        .style('font-size', '14px')
        .style('visibility', 'hidden')
        .text(d => d.label)

    let box = dropdown.append("rect")
        .attr("x", args.x)
        .attr("y", args.y)
        .attr("rx", 3)
        .attr("ry", 3)
        .attr("width", args.width)
        .attr("height", args.height)
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
        .style("text-anchor", "middle")
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
        .interpolate("linear")

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
        .style("text-anchor", "middle")
        .style('fill', '#000000')
        .style('font-size', '9px')
        .text(args.min)

    slider.append('text')
        .attr('x', args.x + args.width)
        .attr('y', args.y + args.height + 10)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style("text-anchor", "middle")
        .style('fill', '#000000')
        .style('font-size', '9px')
        .text(args.max)

    slider.append('text')
        .attr('x', args.x + args.width / 2)
        .attr('y', args.y + args.height)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style("text-anchor", "middle")
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
        .on("drag", move)
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
        .attr("cx", sliderScale(args.init))
        .attr("cy", args.y + 10)
        .attr("r", 6)
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
        .style("text-anchor", "start")
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
    return mouseYPos + $("#index").parent().scrollTop() - $('#etrikspanel').offset().top
}

function showCohortInfo(){
    let cohortsSummary = ''

    for(let i = 1; i <= GLOBAL.NumOfSubsets; i++) {
        let currentQuery = getQuerySummary(i)
        if(currentQuery !== '') {
            cohortsSummary += "<br/>Subset " + i + ": <br/>"
            cohortsSummary += currentQuery
            cohortsSummary += "<br/>"
        }
    }
    if (cohortsSummary === '') {
        cohortsSummary = '<br/>WARNING: No subsets have been selected! Please go to the "Comparison" tab and select your subsets.'
    }
    $('#cohortInfo').html(cohortsSummary)
}
showCohortInfo()

function updateInputView() {
    if (typeof updateOnView === "function") {
        updateOnView()
    }
}

$('#resultsTabPanel__etrikspanel').click(showCohortInfo)
$('#resultsTabPanel__etrikspanel').click(updateInputView)

function getMaxWidth(elements) {
    const MIN_SAFE_INTEGER = -(Math.pow(2, 53) - 1)
    let currentMax = MIN_SAFE_INTEGER
    elements.each(() => {
        let len = this.getBBox().width
        if (len > currentMax) {
            currentMax = len
        }
    })
    return currentMax
}

function createQueryCriteriaDIV({conceptid, normalunits, setvaluemode, setvalueoperator, setvaluelowvalue, setvaluehighvalue, setvalueunits, oktousevalues, setnodetype}) {
    return {
        conceptid : conceptid,
        conceptname : shortenConcept(conceptid),
        concepttooltip : conceptid.substr(1, conceptid.length),
        conceptlevel : '',
        concepttablename : "CONCEPT_DIMENSION",
        conceptdimcode : conceptid,
        conceptcomment : "",
        normalunits : normalunits,
        setvaluemode : setvaluemode,
        setvalueoperator : setvalueoperator,
        setvaluelowvalue : setvaluelowvalue,
        setvaluehighvalue : setvaluehighvalue,
        setvaluehighlowselect : "N",
        setvalueunits : setvalueunits,
        oktousevalues : oktousevalues,
        setnodetype : setnodetype,
        visualattributes : "LEAF,ACTIVE",
        applied_path : "@",
        modifiedNodePath : "undefined",
        modifiedNodeId : "undefined",
        modifiedNodeLevel : "undefined"
    }
}

function setCohorts({constrains, andConcat, negate, reCompute, subset=1}) {
    if (typeof appendItemFromConceptInto !== "function") {
        alert('This functionality is not available in the tranSMART version you use.')
        return
    }
    if (! confirm("Attention! This action will have the following impact:\n1. Your cohort selection in the 'Comparison' tab will be modified.\n2. Your current analysis will be recomputed based on this selection.\n")) {
        return
    }

    let destination = $($("#queryTable tr:last-of-type td")[subset - 1]).find('div[id^=panelBoxList]').last()
    for (let constrain of constrains) {
        if (andConcat) {
            destination = $($("#queryTable tr:last-of-type td")[subset - 1]).find('div[id^=panelBoxList]').last()
        }
        appendItemFromConceptInto(destination, constrain, negate)
    }
    if (reCompute) {
        runAllQueries(runAnalysis)
    }
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
    let div = Ext.get(divName).dom
    while (div.firstChild) {
        div.removeChild(div.firstChild)
    }
}

function getConcepts(divName) {
    return $('#' + divName).children().toArray().map(childNode => childNode.getAttribute('conceptid'))
}

function addSettingsToData(data, settings) {
    for (let element of data) {
        if (element.name == "settings") {
            let json = JSON.parse(element.value)
            json = $.extend(json, settings)
            element.value = JSON.stringify(json)
            break
        }
    }
    return data
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

function prepareFormData() {
    let data = []
    data.push({name: 'conceptBoxes', value: JSON.stringify(conceptBoxes)})
    data.push({name: 'result_instance_id1', value: GLOBAL.CurrentSubsetIDs[1]})
    data.push({name: 'result_instance_id2', value: GLOBAL.CurrentSubsetIDs[2]})
    data.push({name: 'script', value: $('#scriptSelect').val()})
    data.push({name: 'settings', value: JSON.stringify(getSettings())})
    data.push({name: 'cookieID', value: setSmartRCookie()})
    return data
}

function containsOnly(divName, icon) {
    // FIXME: the part after || is only because alphaicon does not exist in the current master branch
    return $('#' + divName).children().toArray().every(childNode => childNode.getAttribute('setnodetype') === icon || icon === 'alphaicon');
}

function sane() { // FIXME: somehow check for subset2 to be non empty iff two cohorts are needed
    if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
        alert('No cohorts have been selected. Please drag&drop cohorts to the fields within the "Comparison" tab')
        return false
    }

    if ($("#scriptSelect").val() === '') {
        alert('Please select the algorithm you want to use!')
        return false
    }

    for (let sanityCheckError of sanityCheckErrors) {
        if (sanityCheckError !== '') {
            alert(sanityCheckError)
            return false
        }
    }
    return customSanityCheck() // method MUST be implemented by _inFoobarAnalysis.gsp
}

function setSmartRCookie() {
    let cookies = document.cookie.split('')
    var cookie = cookies.find(cookie => cookie.split('=')[0] === 'SmartR')
    if (cookie) return cookie.split('=')[1]
    let id = new Date().getTime() + Math.floor((Math.random() * 9999999999) + 1000000000)
    document.cookie = 'SmartR=' + id
    return id
}

function setImage(divName, image) {
    function _arrayBufferToBase64( buffer ) {
        let binary = ''
        let bytes = new Uint8Array( buffer )
        let len = bytes.byteLength
        for (let byte of bytes) {
            binary += String.fromCharCode(byte)
        }
        return window.btoa( binary )
    }

    let img = document.createElement('img')
    img.setAttribute('src', "data:image/pngbase64," + _arrayBufferToBase64(image))
    document.getElementById(divName).appendChild(img)
}

function goToEAE() {
    $.ajax({
        url: pageInfo.basePath + '/SmartR/goToEAEngine' ,
        type: "POST",
        timeout: '600000'
    }).done(response => {
        $("#index").html(response)
    }).fail(() => {
        $("#index").html("AJAX CALL FAILED!")
    })
}

function renderResultsInTemplate(callback, data) {
    $.ajax({
        url: pageInfo.basePath + '/SmartR/renderResultsInTemplate',
        type: "POST",
        timeout: 1.8e+6,
        data: data
    }).done(response => {
        if (response === 'RUNNING') {
            setTimeout(renderResultsInTemplate(callback, data), 5000)
        } else {
            $('#submitButton').prop('disabled', false)
            callback()
            $("#outputDIV").html(response)
        }
    }).fail(() => {
        $('#submitButton').prop('disabled', false)
        callback()
        $("#outputDIV").html("Could not render results. Please contact your administrator.")
    })
}

function renderResults(callback, data) {
    $.ajax({
        url: pageInfo.basePath + '/SmartR/renderResults',
        type: "POST",
        timeout: 1.8e+6,
        data: data
    }).done(response => {
        response = JSON.parse(response)
        if (response.error === 'RUNNING') {
            setTimeout(renderResults(callback, data), 5000)
        } else if (response.error) {
            $('#submitButton').prop('disabled', false)
            alert(response.error)
        } else {
            $('#submitButton').prop('disabled', false)
            callback(response)
        }
    }).fail(() => {
        $('#submitButton').prop('disabled', false)
        alert("Server does not respond. Network connection lost?")
    })
}

function computeResults(callback=()=>{}, data=prepareFormData(), init=true, redraw=true) {
    const retCodes = {
        1: 'An unexpected error occured while initializing environment.',
        2: 'An unexpected error occured while accessing the database.',
        3: 'An unexpected error occured while processing the data.'
    }

    $('#submitButton').prop('disabled', true)
    $.ajax({
        url: pageInfo.basePath + '/SmartR/' + (init ? 'computeResults' : 'reComputeResults'),
        type: "POST",
        timeout: 1.8e+6,
        data: data
    }).done(response => {
        if (response === '0') { // successful
            if (redraw) {
                renderResultsInTemplate(callback, data)
            } else {
                renderResults(callback, data)
            }
        } else {
            if (init) {
                $("#outputDIV").html('')
            }
            $('#submitButton').prop('disabled', false)
            alert(retCodes[response])
        }
    }).fail(() => {
        if (redraw) {
            renderResultsInTemplate(callback, data)
        } else {
            renderResults(callback, data)
        }
    })
}

function showLoadingScreen() {
    $.ajax({
        url: pageInfo.basePath + '/SmartR/renderLoadingScreen',
        type: "POST",
        timeout: 1.8e+6
    }).done(response => {
        $("#outputDIV").html(response)
    }).fail(() => {
        $("#outputDIV").html("Loading screen could not be initialized. Probably you lost network connection.")
    })
}

function runAnalysis() {
    conceptBoxes = []
    sanityCheckErrors = []
    register() // method MUST be implemented by _inFoobarAnalysis.gsp
    if (! sane()) return false
    // if no subset IDs exist compute them
    if(!(isSubsetEmpty(1) || GLOBAL.CurrentSubsetIDs[1]) || !( isSubsetEmpty(2) || GLOBAL.CurrentSubsetIDs[2])) {
        runAllQueries(runAnalysis)
        return false
    }
    showLoadingScreen()
    computeResults()
}

function changeInputDIV() {
    $("#outputDIV").html("")
    $.ajax({
        url: pageInfo.basePath + '/SmartR/renderInputDIV',
        type: "POST",
        timeout: 1.8e+6,
        data: {'script': $('#scriptSelect').val()}
    }).done(response => {
        $("#inputDIV").html(response)
        updateInputView()
    }).fail(() => {
        $("#inputDIV").html("Coult not render input form. Probably you lost network connection.")
    })
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
