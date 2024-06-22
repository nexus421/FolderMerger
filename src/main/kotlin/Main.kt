import java.io.File
import kotlin.system.measureTimeMillis

var count = 0
var error = 0

fun main() {

    println("FileMerger by Marvin with Kotlin <3\n")

    println("Enter path to the root working folder. Keep empty for current directory.")
    print("Path: ")
    val pathToRootFolder = readlnOrNull().let {
        if (it.isNullOrBlank()) {
            println("Using default. Current Directory.\n")
            ".${File.separator}"
        } else {
            println("Using entered path: $it\n")
            if (File(it).exists().not()) {
                System.err.println("Path does not exist. Exit.")
                return
            }
            it
        }
    }

    print("Do you want to delete empty folders and copied files? (y/n): ")
    val deleteCopiedFilesAndFolders = readlnOrNull().equals("y", true)
    println(if (deleteCopiedFilesAndFolders) "Delete empty folders and copied files.\n" else "Do not delete anything.\n")

    print("Do you want to log all copied files? (y/n)? (Errors will always be logged.): ")
    val logAllCopiedFiles = readlnOrNull().equals("y", true)
    println(if (logAllCopiedFiles) "Log everything.\n" else "Log only errors and other important things.\n")

    print("Do you want to ignore files in the execution directory and only search in sub folders? (y/n): ")
    val ignoreFilesInRoot = readlnOrNull().equals("y", true)
    println(if (ignoreFilesInRoot) "Ignoring files in root.\n" else "Copy all files in the given path.\n")

    print("Ignore duplicate entries (y) or add tag behind duplicated files (n): ")
    val ignoreDuplicateEntries = readlnOrNull().equals("y", true)
    println(if (ignoreDuplicateEntries) "Duplicate files will be ignored." else "Keep duplicate files and tag them.\n")

    print("Enter all file endings without the dot which should be ignored. Keep empty for nothing. Split with \",\": ")
    val ignoreFileEndings: List<String> =
        readlnOrNull()?.split(",")?.map { it.trim().lowercase() }?.filter { it.isNotEmpty() } ?: emptyList()
    println()

    print("Dry run without real copy or delete (y/n): ")
    val dryRun = readlnOrNull().equals("y", true)
    println(if (dryRun) "Dry run. Nothing will really happen.\n" else "Do what we want to do!\n")

    val rootFolder = File(pathToRootFolder)
    val files = rootFolder.listFiles()?.let {
        if (ignoreFilesInRoot) it.filter { file -> file.isDirectory }.toTypedArray()
        else it
    } ?: emptyArray()

    val newPlace = File(rootFolder, "MergedResult").apply {
        mkdir()
    }

    println("The result will be stored at ${newPlace.absolutePath}")
    println("Starting in... (cancel with Strg + C)")
    wait(3)
    println()

    val time = measureTimeMillis {
        copyFilesToMerged(
            files,
            newPlace,
            deleteCopiedFilesAndFolders,
            logAllCopiedFiles,
            dryRun,
            ignoreDuplicateEntries,
            ignoreFileEndings
        )
    }

    if (dryRun) println("\nExecuted as dryRun. No files were really copied or deleted!")
    println("\nCopied $count files in $time ms with $error errors")
}

fun copyFilesToMerged(
    files: Array<File>,
    newPlace: File,
    delete: Boolean,
    logCopies: Boolean,
    dryRun: Boolean,
    ignoreDuplicateEntries: Boolean,
    ignoreFileEndings: List<String>
) {
    files.forEach { file ->
        try {

            if (file.isDirectory) {
                file.listFiles()?.let {
                    copyFilesToMerged(
                        it,
                        newPlace,
                        delete,
                        logCopies,
                        dryRun,
                        ignoreDuplicateEntries,
                        ignoreFileEndings
                    )
                }
                if (dryRun.not() && delete && file.listFiles()?.isEmpty() == true) file.deleteRecursively()
            } else if (file.isFile) {
                if (ignoreFile(file, ignoreFileEndings)) return@forEach

                val newDestination = File(newPlace, file.name).let {
                    if (it.exists()) {
                        if (ignoreDuplicateEntries) {
                            println("Ignoring duplicated file from ${file.absolutePath}")
                            it
                        } else File(newPlace, "duplicate_${System.nanoTime()}_" + file.name)
                    } else it
                }

                //Kopiert die Datei an das neue Ziel
                if (dryRun.not()) file.copyTo(newDestination)
                count++

                //Löscht die Datei, falls Flag aktiv
                if (dryRun.not() && delete) file.delete()

                //Info-Log, falls aktiviert
                if (logCopies) println("Copy file to new place (${newDestination.name}). ${if (delete) "Deleted file." else ""}")
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            error++
        }
    }
}

/**
 * Checks if the given file ends with any of the specified file endings.
 *
 * @param file The file to be checked.
 * @param ignoreFileEndings The list of file endings to be ignored.
 * @return `true` if the file ends with any of the specified file endings, `false` otherwise.
 */
fun ignoreFile(file: File, ignoreFileEndings: List<String>) =
    ignoreFileEndings.any { file.name.lowercase().endsWith(it) }

/**
 * Waits for the specified number of seconds.
 *
 * @param seconds The number of seconds to wait.
 */
fun wait(seconds: Int) {
    (0..seconds).forEach {
        try {
            println(seconds - it)
            Thread.sleep(1000)
        } catch (e: Exception) {
            System.err.println("Error waiting: ${e.message}")
        }
    }
}