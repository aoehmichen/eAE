modules = {
    global {
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jquery-2.1.4.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'polyfill.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'd3.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR', file: 'smartR-compiled.js']
        resource url: [plugin: 'smart-r', dir: 'css', file: 'smartR.css']
    }

    volcano_analysis {
        resource url: [plugin: 'smart-r', dir: 'js/smartR', file: 'VolcanoAnalysis-compiled.js']
        resource url: [plugin: 'smart-r', dir: 'css', file: 'VolcanoAnalysis.css']
    }

    correlation_analysis {
        resource url: [plugin: 'smart-r', dir: 'js/smartR', file: 'CorrelationAnalysis-compiled.js']
        resource url: [plugin: 'smart-r', dir: 'css', file: 'CorrelationAnalysis.css']
    }
}