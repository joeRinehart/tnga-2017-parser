import groovy.json.JsonOutput

final BigDecimal ORIGINAL_TSV_SUM = 367.40

// raw list for tabular output
List cues = []
// structured format for structured output
List segments = []
Map currentSegment = null

new File('../in/tnga-2017-raw.tsv')
    .findAll { String line ->
        line.trim().length()
    }.collect { String line ->
        line.trim()
    }
    .each{ String line ->
        if ( line.startsWith('DIRECTIONS') ) {
            //no-op
        } else if ( line.startsWith('BEGIN') ) {
            segments << [ name: line.tokenize('\t')[1], cues: [] ]
            currentSegment = segments.last()
        } else {
            List tokens = [currentSegment.name, *line.tokenize('\t')]
            // handle segment ends
            tokens[3] = tokens[3].toString().startsWith('END') ? 0 : tokens[3]
            currentSegment.cues << [ at: tokens[1], cue: tokens[2], for: tokens[3] ]
            cues << [ segment: currentSegment.name ] + currentSegment.cues.last()
        }
    }

// json
String json = JsonOutput.toJson([
        segments: segments,
        integrity: [
                originalTsvMileageSum: ORIGINAL_TSV_SUM,
                parsedMilageSum: cues.sum{ new BigDecimal(it.for) }
        ]
])
new File('../out/tnga-2017.json').write(JsonOutput.prettyPrint(json))

// tsv
File outFile = new File('../out/tnga-2017.txt')
outFile.write('segment\tat\tcue\tfor\n')
cues.each{ cue ->
    outFile.append(cue.collect{ it.value }.join('\t') + '\n')
}

// separated tsv
outFile = new File('../out/tnga-2017-by-segment.txt')
outFile.write('')
segments.each{ segment ->
    outFile.append([segment.name,'',''].join('\t') + '\n')
    outFile.append('at\tcue\tfor\n')
    segment.cues.each{ cue ->
        outFile.append(cue.collect{ it.value }.join('\t') + '\n')
    }
    outFile.append(['','',''].join('\t') + '\n')
}






