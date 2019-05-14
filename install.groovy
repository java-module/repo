
def prefix = "https://github.com/java-module/repo/raw/master/"
def repoPath = "repo/"

def applyList = [
        "meta/io.groovy",
        "meta/modules.groovy",
] as Collection<String>

def downloadList = applyList + [
        "install.groovy",
] as Collection<String>

def downloadMap = downloadList.collectEntries {
    //println "map: $it"
    new MapEntry(it, new File(rootDir, "$repoPath$it"))
} as Map<String, File>

downloadMap.each { item ->

    //println "install $item.key, file://$item.value"

    item.value.with {
        if (!exists()) {
            parentFile.mkdirs()
            withOutputStream { outStream ->
                new URL("${prefix}${item.key}").withInputStream { inStream ->
                    outStream << inStream
                }
            }
            println "install: $item.key, file://$item.value"
        }
    }

    if (applyList.contains(item.key)) {
        println "apply from: $repoPath$item.key"
        apply from: item.value
    }
}