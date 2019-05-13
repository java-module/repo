import groovy.io.FileType

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

URL.metaClass.download = { File destination, Proxy proxy = null ->
    def connection = proxy == null ? openConnection() : openConnection(proxy) as HttpURLConnection
    connection.with {
        connect()
        println "download $destination.name, $responseCode, $responseMessage, $url"
        if (responseCode == 200) {

            inputStream.withStream { inStream ->
                destination.withOutputStream { outStream ->
                    outStream << inStream
                }
            }

            println "download finish file://${destination.canonicalPath}, $url"
        } else if (responseCode == 302 || responseCode == 301) {
            def location = getHeaderField("Location")
            println "$responseCode, $location"
            new URL(location).download(destination)
        }
        responseCode
    }
}

File.metaClass.zip = { File destination, boolean overwrite = false ->

    if (overwrite) destination.deleteDir()
    def result = new ZipOutputStream(new FileOutputStream(destination))
    result.withStream { outStream ->

        def prefix = delegate.parentFile != null ? delegate.parentFile.canonicalPath.size() : 0
        delegate.eachFileRecurse(FileType.FILES) { inFile ->

            println "zip file://$inFile.canonicalPath"
            def entryName = inFile.canonicalPath.substring(prefix)
            outStream.putNextEntry(new ZipEntry(entryName))
            inFile.withInputStream { inStream ->
                outStream << inStream
            }
            outStream.closeEntry()
        }
    }
}

File.metaClass.unzip = { File destination, boolean overwrite = false ->

    if (overwrite) destination.deleteDir()
    def zip = new ZipFile(delegate)
    zip.entries().findAll { !it.directory }.each { ZipEntry entry ->

        def outFile = new File(destination, entry.name)
        println "unzip file://$outFile.canonicalPath"
        if (outFile.parentFile != null)
            outFile.parentFile.mkdirs()
        outFile.withOutputStream { outStream ->

            zip.getInputStream(entry).withStream { inStream ->
                outStream << inStream
            }
        }
    }
}