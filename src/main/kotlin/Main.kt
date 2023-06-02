import java.io.File
import kotlin.system.measureTimeMillis

var count = 0
var error = 0

fun main() {

    println("FileMerger by Marvin with Kotlin <3\n")

    print("Enter path to execution place. Default ist current directory, keep empty for default. ")
    val pathToRootFolder = readlnOrNull().let {
        if(it.isNullOrBlank()) {
            println("Using default. Current Directory.\n")
            ".${File.separator}"
        } else {
            println("Using entered path: $it")
            if(File(it).exists().not()) {
                System.err.println("Path does not exist. Exit.")
                return
            }
            it
        }
    }

    print("Do you want to delete empty folders and copied files? (y/n): ")
    val deleteCopiedFilesAndFolders = readlnOrNull().equals("y", true)
    println(if(deleteCopiedFilesAndFolders) "Delete empty folders and copied files.\n" else "Do not delete anything.\n")

    print("Do you want to log all copied files? (y/n)? (Error will always be logged.): ")
    val logAllCopiedFiles = readlnOrNull().equals("y", true)
    println(if(logAllCopiedFiles) "Log everything.\n" else "Log only errors and other important things.\n")

    print("Do you want to ignore files in the execution directory and only search in sub folders? (y/n): ")
    val ignoreFilesInRoot = readlnOrNull().equals("y", true)
    println(if(logAllCopiedFiles) "Ignoring files in root.\n" else "Copy all files in the given path.\n")

    print("Dry run without real copy or delete (y/n): ")
    val dryRun = readlnOrNull().equals("y", true)
    println(if(logAllCopiedFiles) "Dry run. Nothing will really happen.\n" else "Do what we want to do!\n")

    println("Starting in... (cancel with Strg + C)")
    wait(3)
    println()

    val root = File(pathToRootFolder)
    val files = root.listFiles()?.let {
        if(ignoreFilesInRoot) it.filter { file -> file.isDirectory }.toTypedArray()
        else it
    } ?: emptyArray()
    val newPlace = File(root, "MergedImages").apply {
        mkdir()
    }

    val time = measureTimeMillis {
        copyFilesToMerged(files, newPlace, deleteCopiedFilesAndFolders, logAllCopiedFiles, dryRun)
    }

    if(dryRun) println("\nExecuted as dryRun. No files were really copied or deleted!")
    println("\nCopied $count files in $time ms with $error errors")
}

fun copyFilesToMerged(files: Array<File>, newPlace: File, delete: Boolean, logCopies: Boolean, dryRun: Boolean) {
    files.forEach {file ->
        if(file.isDirectory) {
            file.listFiles()?.let {
                copyFilesToMerged(it, newPlace, delete, logCopies, dryRun)
            }
            if(dryRun.not() && delete && file.listFiles()?.isEmpty() == true) file.deleteRecursively()
        } else if(file.isFile) {
            try {
                if(ignore(file)) return@forEach
                if(dryRun.not()) file.copyTo(File(newPlace, file.name))
                count++
                if(dryRun.not() && delete) file.delete()
                if(logCopies) println("Copy file to new place (${file.name}). ${if(delete) "Deleted file." else ""}")
            } catch (faee: FileAlreadyExistsException) {
                System.err.println("File already exists (${file.absolutePath}). Ignored.")
                error++
            }
        }
    }
}

fun ignore(file: File): Boolean  {
    with(file.name) {
        return when {
            equals(".DS_Store", true) -> true
            contains(".jar") -> true
            else -> false
        }
    }
}

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