
def prefix = ""
def repoPath = "repo/"

def applyList = [
        "meta/io.groovy",
        "meta/modules.groovy",
] as Collection<String>

def downloadList = applyList + [
        "install.groovy",
] as Collection<String>

def downloadMap = downloadList.collectEntries {
    //println "map: $repoPath$it"
    new MapEntry("$repoPath$it", new File(rootDir, "$repoPath$it"))
} as Map<String, File>

downloadMap.each { item ->

    //println "install $repoPath$item.key, file://$item.value"

    item.value.with {
        if (!exists()) {
            withOutputStream { outStream ->
                new URL("${prefix}reop/$item.key").withInputStream { inStream ->
                    outStream << inStream
                }
            }
            println "install: $repoPath$item.key, file://$item.value"
        }
    }

    if (applyList.contains(item.key)) {
        println "apply from: $repoPath$item.key"
        apply from: item.value
    }
}