'use strict';

function createD3Button(args) {
    var button = args.location.append('g');

    var box = button.append('rect').attr('x', args.x).attr('y', args.y).attr('rx', 3).attr('ry', 3).attr('width', args.width).attr('height', args.height).style('stroke-width', '1px').style('stroke', '#009ac9').style('fill', '#009ac9').style('cursor', 'pointer').on('mouseover', function () {
        box.transition().duration(300).style('fill', '#ffffff');

        text.transition().duration(300).style('fill', '#009ac9');
    }).on('mouseout', function () {
        box.transition().duration(300).style('fill', '#009ac9');

        text.transition().duration(300).style('fill', '#ffffff');
    }).on('click', function () {
        return args.callback();
    });

    var text = button.append('text').attr('x', args.x + args.width / 2).attr('y', args.y + args.height / 2).attr('dy', '0.35em').style('pointer-events', 'none').style('text-anchor', 'middle').style('fill', '#ffffff').style('font-size', '14px').text(args.label);

    return button;
}

function createD3Switch(args) {
    var switcher = args.location.append('g');

    var checked = args.checked;
    var color = checked ? 'green' : 'red';

    var box = switcher.append('rect').attr('x', args.x).attr('y', args.y).attr('rx', 3).attr('ry', 3).attr('width', args.width).attr('height', args.height).style('stroke-width', '1px').style('stroke', color).style('fill', color).style('cursor', 'pointer').on('click', function () {
        if (color === 'green') {
            box.transition().duration(300).style('stroke', 'red').style('fill', 'red');
            color = 'red';
            checked = false;
        } else {
            box.transition().duration(300).style('stroke', 'green').style('fill', 'green');
            color = 'green';
            checked = true;
        }
        text.text(checked ? args.onlabel : args.offlabel);
        args.callback(checked);
    });

    var text = switcher.append('text').attr('x', args.x + args.width / 2).attr('y', args.y + args.height / 2).attr('dy', '0.35em').style('pointer-events', 'none').style('text-anchor', 'middle').style('fill', '#ffffff').style('font-size', '14px').text(checked ? args.onlabel : args.offlabel);

    return switcher;
}

function createD3Dropdown(args) {
    function shrink() {
        dropdown.selectAll('.itemBox').attr('y', args.y + args.height).style('visibility', 'hidden');
        dropdown.selectAll('.itemText').attr('y', args.y + args.height + args.height / 2).style('visibility', 'hidden');
        itemHovered = false;
        hovered = false;
        itemHovered = false;
    }
    var dropdown = args.location.append('g');

    var hovered = false;
    var itemHovered = false;

    var itemBox = dropdown.selectAll('.itemBox').data(args.items, function (item) {
        return item.label;
    });

    itemBox.enter().append('rect').attr('class', 'itemBox').attr('x', args.x).attr('y', args.y + args.height).attr('rx', 0).attr('ry', 0).attr('width', args.width).attr('height', args.height).style('cursor', 'pointer').style('stroke-width', '2px').style('stroke', '#ffffff').style('fill', '#E3E3E3').style('visibility', 'hidden').on('mouseover', function () {
        itemHovered = true;
        d3.select(this).style('fill', '#009ac9');
    }).on('mouseout', function () {
        itemHovered = false;
        d3.select(this).style('fill', '#E3E3E3');
        setTimeout(function () {
            if (!hovered && !itemHovered) {
                shrink();
            }
        }, 50);
    }).on('click', function (d) {
        return d.callback();
    });

    var itemText = dropdown.selectAll('.itemText').data(args.items, function (item) {
        return item.label;
    });

    itemText.enter().append('text').attr('class', 'itemText').attr('x', args.x + args.width / 2).attr('y', args.y + args.height + args.height / 2).attr('dy', '0.35em').style('pointer-events', 'none').style('text-anchor', 'middle').style('fill', '#000000').style('font-size', '14px').style('visibility', 'hidden').text(function (d) {
        return d.label;
    });

    var box = dropdown.append('rect').attr('x', args.x).attr('y', args.y).attr('rx', 3).attr('ry', 3).attr('width', args.width).attr('height', args.height).style('stroke-width', '1px').style('stroke', '#009ac9').style('fill', '#009ac9').on('mouseover', function () {
        if (hovered) {
            return;
        }
        dropdown.selectAll('.itemBox').transition().duration(300).style('visibility', 'visible').attr('y', function (d) {
            var idx = args.items.findIndex(function (item) {
                return item.label === d.label;
            });
            return 2 + args.y + (idx + 1) * args.height;
        });

        dropdown.selectAll('.itemText').transition().duration(300).style('visibility', 'visible').attr('y', function (d) {
            var idx = args.items.findIndex(function (item) {
                return item.label === d.label;
            });
            return 2 + args.y + (idx + 1) * args.height + args.height / 2;
        });

        hovered = true;
    }).on('mouseout', function () {
        hovered = false;
        setTimeout(function () {
            if (!hovered && !itemHovered) {
                shrink();
            }
        }, 50);
        setTimeout(function () {
            // first check is not enough if animation interrupts it
            if (!hovered && !itemHovered) {
                shrink();
            }
        }, 350);
    });

    var text = dropdown.append('text').attr('class', 'buttonText').attr('x', args.x + args.width / 2).attr('y', args.y + args.height / 2).attr('dy', '0.35em').style('pointer-events', 'none').style('text-anchor', 'middle').style('fill', '#ffffff').style('font-size', '14px').text(args.label);

    return dropdown;
}

function createD3Slider(args) {
    var slider = args.location.append('g');

    var lineGen = d3.svg.line().x(function (d) {
        return d.x;
    }).y(function (d) {
        return d.y;
    }).interpolate('linear');

    var lineData = [{ x: args.x, y: args.y + args.height }, { x: args.x, y: args.y + 0.75 * args.height }, { x: args.x + args.width, y: args.y + 0.75 * args.height }, { x: args.x + args.width, y: args.y + args.height }];

    var sliderScale = d3.scale.linear().domain([args.min, args.max]).range([args.x, args.x + args.width]);

    slider.append('path').attr('d', lineGen(lineData)).style('pointer-events', 'none').style('stroke', '#009ac9').style('stroke-width', '2px').style('shape-rendering', 'crispEdges').style('fill', 'none');

    slider.append('text').attr('x', args.x).attr('y', args.y + args.height + 10).attr('dy', '0.35em').style('pointer-events', 'none').style('text-anchor', 'middle').style('fill', '#000000').style('font-size', '9px').text(args.min);

    slider.append('text').attr('x', args.x + args.width).attr('y', args.y + args.height + 10).attr('dy', '0.35em').style('pointer-events', 'none').style('text-anchor', 'middle').style('fill', '#000000').style('font-size', '9px').text(args.max);

    slider.append('text').attr('x', args.x + args.width / 2).attr('y', args.y + args.height).attr('dy', '0.35em').style('pointer-events', 'none').style('text-anchor', 'middle').style('fill', '#000000').style('font-size', '14px').text(args.label);

    var currentValue = args.init;

    function move() {
        var xPos = d3.event.x;
        if (xPos < args.x) {
            xPos = args.x;
        } else if (xPos > args.x + args.width) {
            xPos = args.x + args.width;
        }

        currentValue = Number(sliderScale.invert(xPos)).toFixed(5);

        dragger.attr('x', xPos - 20);
        handle.attr('cx', xPos);
        pointer.attr('x1', xPos).attr('x2', xPos);
        value.attr('x', xPos + 10).text(currentValue);
    }

    var drag = d3.behavior.drag().on('drag', move).on(args.trigger, function () {
        args.callback(currentValue);
    });

    var dragger = slider.append('rect').attr('x', sliderScale(args.init) - 20).attr('y', args.y).attr('width', 40).attr('height', args.height).style('opacity', 0).style('cursor', 'pointer').on('mouseover', function () {
        handle.style('fill', '#009ac9');
        pointer.style('stroke', '#009ac9');
    }).on('mouseout', function () {
        handle.style('fill', '#000000');
        pointer.style('stroke', '#000000');
    }).call(drag);

    var handle = slider.append('circle').attr('cx', sliderScale(args.init)).attr('cy', args.y + 10).attr('r', 6).style('pointer-events', 'none').style('fill', '#000000');

    var pointer = slider.append('line').attr('x1', sliderScale(args.init)).attr('y1', args.y + 10).attr('x2', sliderScale(args.init)).attr('y2', args.y + 0.75 * args.height).style('pointer-events', 'none').style('stroke', '#000000').style('stroke-width', '1px');

    var value = slider.append('text').attr('x', sliderScale(args.init) + 10).attr('y', args.y + 10).attr('dy', '0.35em').style('pointer-events', 'none').style('text-anchor', 'start').style('fill', '#000000').style('font-size', '10px').text(args.init);

    return slider;
}

function mouseX() {
    var mouseXPos = typeof d3.event.sourceEvent !== 'undefined' ? d3.event.sourceEvent.pageX : d3.event.clientX;
    return mouseXPos - $('#etrikspanel').offset().left + $('#index').parent().scrollLeft();
}

function mouseY() {
    var mouseYPos = typeof d3.event.sourceEvent !== 'undefined' ? d3.event.sourceEvent.pageY : d3.event.clientY;
    return mouseYPos + $('#index').parent().scrollTop() - $('#etrikspanel').offset().top;
}

function getMaxWidth(selection) {
    return selection[0].map(function (d) {
        return d.getBBox().width;
    }).max();
}

function showCohortInfo() {
    var cohortsSummary = '';

    var _iteratorNormalCompletion = true;
    var _didIteratorError = false;
    var _iteratorError = undefined;

    try {
        for (var _iterator = Array(GLOBAL.NumOfSubsets).keys()[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
            var i = _step.value;

            var currentQuery = getQuerySummary(i + 1);
            if (currentQuery !== '') {
                cohortsSummary += '<br/>Subset ' + (i + 1) + ': <br/>';
                cohortsSummary += currentQuery;
                cohortsSummary += '<br/>';
            }
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

    if (!cohortsSummary) {
        cohortsSummary = '<br/>WARNING: No subsets have been selected! Please go to the "Comparison" tab and select your subsets.';
    }
    $('#cohortInfo').html(cohortsSummary);
}
showCohortInfo();

function updateInputView() {
    if (typeof updateOnView === 'function') {
        updateOnView();
    }
}

var panelItem = $('#resultsTabPanel__etrikspanel');
panelItem.click(showCohortInfo);
panelItem.click(updateInputView);

function shortenConcept(concept) {
    var splits = concept.split('\\');
    return splits[splits.length - 3] + '/' + splits[splits.length - 2];
}

function activateDragAndDrop(divName) {
    var div = Ext.get(divName);
    var dtgI = new Ext.dd.DropTarget(div, { ddGroup: 'makeQuery' });
    dtgI.notifyDrop = dropOntoCategorySelection;
}

function clearVarSelection(divName) {
    $('#' + divName).children().remove();
}

function getConcepts(divName) {
    return $('#' + divName).children().toArray().map(function (childNode) {
        return childNode.getAttribute('conceptid');
    });
}

var conceptBoxes = [];
var sanityCheckErrors = [];
function registerConceptBox(name, cohorts) {
    var type = arguments.length <= 2 || arguments[2] === undefined ? 'valueicon' : arguments[2];
    var min = arguments.length <= 3 || arguments[3] === undefined ? 0 : arguments[3];
    var max = arguments.length <= 4 || arguments[4] === undefined ? Number.MAX_SAFE_INTEGER : arguments[4];

    var concepts = getConcepts(name);
    var check1 = containsOnly(name, type);
    var check2 = concepts.length >= min;
    var check3 = concepts.length <= max;
    sanityCheckErrors.push(!check1 ? 'Concept box (' + name + ') contains concepts with invalid type! Valid type: ' + type : !check2 ? 'Concept box (' + name + ') contains too few concepts! Valid range: ' + min + ' - ' + max : !check3 ? 'Concept box (' + name + ') contains too many concepts! Valid range: ' + min + ' - ' + max : '');
    conceptBoxes.push({ name: name, cohorts: cohorts, type: type, concepts: concepts });
}

function containsOnly(divName, icon) {
    // FIXME: the part after || is only because alphaicon does not exist in the current master branch
    return $('#' + divName).children().toArray().every(function (childNode) {
        return childNode.getAttribute('setnodetype') === icon || icon === 'alphaicon';
    });
}

function sane() {
    // FIXME: somehow check for subset2 to be non empty iff two cohorts are needed
    if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
        alert('No cohorts have been selected. Please drag&drop cohorts to the fields within the "Comparison" tab');
        return false;
    }

    if (!$('#scriptSelect').val()) {
        alert('Please select the algorithm you want to use!');
        return false;
    }

    var _iteratorNormalCompletion2 = true;
    var _didIteratorError2 = false;
    var _iteratorError2 = undefined;

    try {
        for (var _iterator2 = sanityCheckErrors[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
            var sanityCheckError = _step2.value;

            if (sanityCheckError) {
                alert(sanityCheckError);
                return false;
            }$;
        }
    } catch (err) {
        _didIteratorError2 = true;
        _iteratorError2 = err;
    } finally {
        try {
            if (!_iteratorNormalCompletion2 && _iterator2.return) {
                _iterator2.return();
            }
        } finally {
            if (_didIteratorError2) {
                throw _iteratorError2;
            }
        }
    }

    return customSanityCheck(); // method MUST be implemented by _inFoobarAnalysis.gsp
}

function setSmartRCookie() {
    var cookies = document.cookie.split(';');
    var cookie = cookies.find(function (cookie) {
        return cookie.split('=')[0] === 'SmartR';
    });
    if (cookie) return cookie.split('=')[1];
    var id = new Date().getTime() + Math.floor(Math.random() * 9999999999 + 1000000000);
    document.cookie = 'SmartR=' + id;
    return id;
}

function goToEAE() {
    var request = $.ajax({
        url: pageInfo.basePath + '/SmartR/goToEAEngine',
        type: 'POST',
        timeout: '600000'
    });
    request.done(function (response) {
        return $('#index').html(response);
    });
    request.fail(function () {
        return $('#index').html('AJAX CALL FAILED!');
    });
}

function displayErrorMsg(params) {
    var request = $.ajax({
        url: pageInfo.basePath + '/SmartR/msg',
        type: "POST",
        timeout: 600000,
        data: { id: params.id }
    });
    request.done(function (response) {
        alert(response);
    });
    request.fail(function () {
        return alert('Server does not respond. Network connection lost?');
    });
    request.always(function () {
        return $('#submitButton').prop('disabled', false);
    });
}

function executeOnState(params) {
    var checkFreq = arguments.length <= 1 || arguments[1] === undefined ? -1 : arguments[1];

    var request = $.ajax({
        url: pageInfo.basePath + '/SmartR/state',
        type: 'POST',
        timeout: 600000,
        data: { id: params.id }
    });
    request.done(function (response) {
        switch (response) {
            case 'INIT':
                loadDataIntoSession(params);
                checkFreq = params.init ? 500 : 100;
                break;
            case 'LOADING':
                break;
            case 'LOADED':
                runWorkflowScript(params);
                checkFreq = 100;
                break;
            case 'WORKING':
                break;
            case 'COMPLETE':
                renderResults(params);
                checkFreq = -1;
                break;
            case 'EXIT':
                checkFreq = -1;
                break;
            case 'ERROR':
                displayErrorMsg(params);
                checkFreq = -1;
                break;
            case 'NULL':
                alert('Session has state "NULL". This is a bug and should be reported.');
                checkFreq = -1;
                break;
            default:
                alert('Your session has an unknown state. This a is a bug and should be reported. State: ' + response);
                checkFreq = -1;
                break;
        }
        if (~checkFreq) {
            console.log(response + checkFreq);
            setTimeout(function () {
                return executeOnState(params, checkFreq);
            }, checkFreq);
        } else if (lastRequest) {
            initSession(lastRequest);
        }
    });
    request.fail(function () {
        return alert('Server does not respond. Network connection lost?');
    });
}

function renderResults(params) {
    $('#submitButton').prop('disabled', false);
    var request = $.ajax({
        url: pageInfo.basePath + '/SmartR/results',
        type: 'POST',
        timeout: 600000,
        data: { id: params.id,
            script: params.script,
            redraw: params.redraw }
    });
    request.done(function (response) {
        if (params.redraw) {
            params.callback();
            $('#loadingDIV').empty();
            $('#outputDIV').html(response);
        } else {
            params.callback(JSON.parse(response));
        }
    });
    request.fail(function () {
        return alert('Server does not respond. Network connection lost?');
    });
}

function runWorkflowScript(params) {
    $.ajax({
        url: pageInfo.basePath + '/SmartR/runWorkflowScript',
        type: 'POST',
        timeout: 600000,
        data: { id: params.id, script: params.script }
    });
}

function loadDataIntoSession(params) {
    $.ajax({
        url: pageInfo.basePath + '/SmartR/loadDataIntoSession',
        type: 'POST',
        timeout: 600000,
        data: { id: params.id,
            rIID1: params.rIID1,
            rIID2: params.rIID2,
            conceptBoxes: params.conceptBoxes,
            settings: params.settings,
            init: params.init }
    });
}

function initSession(params) {
    var request = $.ajax({
        url: pageInfo.basePath + '/SmartR/initSession',
        type: 'POST',
        timeout: 600000,
        data: { id: params.id, init: params.init }
    });
    request.done(function () {
        return executeOnState(params);
    });
    request.fail(function () {
        return alert('Server does not respond. Network connection lost?');
    });
}

function showLoadingScreen() {
    $('#outputDIV').empty();
    var request = $.ajax({
        url: pageInfo.basePath + '/SmartR/loadingScreen',
        type: 'POST',
        timeout: 600000
    });
    request.done(function (response) {
        return $('#loadingDIV').html(response);
    });
    request.fail(function () {
        return alert('Server does not respond. Network connection lost?');
    });
}

var lastRequest = undefined;
function startWorkflow() {
    var visualizationCallback = arguments.length <= 0 || arguments[0] === undefined ? function () {} : arguments[0];
    var settings = arguments.length <= 1 || arguments[1] === undefined ? getSettings() : arguments[1];
    var init = arguments.length <= 2 || arguments[2] === undefined ? true : arguments[2];
    var redraw = arguments.length <= 3 || arguments[3] === undefined ? true : arguments[3];

    lastRequest = null;
    if (!sane()) return false;
    if (!(isSubsetEmpty(1) || GLOBAL.CurrentSubsetIDs[1]) || !(isSubsetEmpty(2) || GLOBAL.CurrentSubsetIDs[2])) {
        runAllQueries(startWorkflow);
        return false;
    }

    settings = JSON.stringify($.extend(getSettings(), settings));
    $('#submitButton').prop('disabled', true);
    conceptBoxes = [];
    sanityCheckErrors = [];
    register();

    if (init && redraw) showLoadingScreen();

    var params = {
        id: setSmartRCookie(),
        settings: settings,
        init: init,
        redraw: redraw,
        callback: visualizationCallback,
        rIID1: GLOBAL.CurrentSubsetIDs[1],
        rIID2: GLOBAL.CurrentSubsetIDs[2],
        conceptBoxes: JSON.stringify(conceptBoxes),
        script: $('#scriptSelect').val()
    };

    var request = $.ajax({
        url: pageInfo.basePath + '/SmartR/state',
        type: 'POST',
        timeout: 600000,
        data: { id: params.id }
    });
    request.done(function (response) {
        if (params.init || params.redraw || response == 'NULL' || response == 'EXIT' || response == 'ERROR') {
            initSession(params);
        } else {
            lastRequest = $.extend(true, {}, params);
        }
    });
    request.fail(function () {
        $('#loadingDIV').empty();
        alert('Server does not respond. Network connection lost?');
    });
}

function changeInputDIV() {
    $('#outputDIV').empty();
    var request = $.ajax({
        url: pageInfo.basePath + '/SmartR/inputDIV',
        type: 'POST',
        timeout: 600000,
        data: { script: $('#scriptSelect').val() }
    });
    request.done(function (response) {
        $('#inputDIV').html(response);
        updateInputView();
    });
    request.fail(function () {
        return alert('Server does not respond. Network connection lost?');
    });
}

function contact() {
    var version = 0.5;

    alert('\nBefore reporting a bug...\n... 1. Make sure you use the lastet SmartR version (installed version: ' + version + ')\n... 2. Make sure that all requirements for using SmartR are met\n\nAll relevant information can be found on https://github.com/sherzinger/SmartR\n\nIf you still want to report a bug you MUST include these information:\n\n>>>' + navigator.userAgent + ' SmartR/' + version + '<<<\n\nBug reports -> http://usersupport.etriks.org/\nFeedback -> sascha.herzinger@uni.lu');
}
