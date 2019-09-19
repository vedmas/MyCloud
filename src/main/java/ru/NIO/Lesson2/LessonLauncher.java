package ru.NIO.Lesson2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Random;

public class LessonLauncher {
    public static void main(String[] args) {

        //OperationFiles
        //readFromFileChannel();


        try {
            RandomAccessFile file = new RandomAccessFile(".\\Repository\\Test.txt", "rw");
            final FileChannel channel = file.getChannel();
            String content = "Hallo, Hallo, Hallo";
            ByteBuffer byteBuffer = ByteBuffer.allocate(30);
            byteBuffer.clear();
            byteBuffer.put(content.getBytes());
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()) {
                channel.write(byteBuffer);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //Поиск файла
//        Path path = Paths.get(".\\Repository\\Test.txt");
//        System.out.println(Files.exists(path));
        //Создание новой директории
//        Path newRepo = Paths.get(".\\NewRepository");
//        try {
//            Files.createDirectory(newRepo);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
    //Копирование файла из одной директории в другую
//        Path sourcePath = Paths.get(".\\Repository\\Test.txt");
//        Path targetPath = Paths.get(".\\NewRepository\\Test.txt");
//        try {
//            System.out.println(Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        Path deletePath = Paths.get(".\\NewRepository\\Test.txt");
//        try {
//            Files.delete(deletePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //Обход дерева директорий, вывод всех элементов
//        Path root = Paths.get(".\\Repository");
//        try {
//            Files.walkFileTree(root, new FileVisitor<Path>() {
//                @Override
//                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                    System.out.println("preVisitDirectory= " + dir);
//                    return FileVisitResult.CONTINUE;
//                }
//
//                @Override
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
//                    System.out.println("    visitFile file= " + file);
//                    return FileVisitResult.CONTINUE;
//                }
//
//                @Override
//                public FileVisitResult visitFileFailed(Path file, IOException exc) {
//                    System.out.println("    visitFileFailed file= " + file);
//                    return FileVisitResult.CONTINUE;
//                }
//
//                @Override
//                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
//                    System.out.println("postVisitDirectory= " + dir);
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //Поиск файла в подкаталогах
//        Path root = Paths.get(".\\Repository");
//        //String searchQuery = File.separator.concat("Test1.txt"); // Путь к файлу. separator переводит
//        // разделители в своответствии с ОС(Win, MacOS, Linux)
//        String searchQuery = "Test3.txt";
//        try {
//            Files.walkFileTree(root, new SimpleFileVisitor<Path>(){
//                @Override
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    String filePath = file.toAbsolutePath().toString();
//                    if(filePath.endsWith(searchQuery)) {
//                        System.out.println("searchQuery = " + searchQuery);
//                        System.out.println("It is necessary file. File path = " + filePath);
//                        return FileVisitResult.TERMINATE;
//                    }
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //Удаление всех найденых файлов и каталогов, кроме Repository
//        Path root = Paths.get(".\\Repository");
//        try {
//            Files.walkFileTree(root, new SimpleFileVisitor<>(){
//                @Override
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    System.out.println("File : " + file + " deleted!");
//                    Files.delete(file);
//                    return FileVisitResult.CONTINUE;
//                }
//
//                @Override
//                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                    if(!dir.endsWith(root)) {
//                        System.out.println("File : " + dir + " deleted!");
//                        Files.delete(dir);
//                    }
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static void readFromFileChannel() {
        try {
            RandomAccessFile file = new RandomAccessFile(".\\Repository\\Test.txt", "rw");
            final FileChannel channel = file.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(50);
            int read = channel.read(buf);
            while (read != -1) {
                buf.flip();
                while (buf.hasRemaining()) {
                    System.out.println((char) buf.get());
                }
                buf.clear();
                read = channel.read(buf);
            }
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
