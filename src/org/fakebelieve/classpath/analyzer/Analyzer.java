/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fakebelieve.classpath.analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author mock
 */
public class Analyzer {

    /**
     * @param args the command line arguments
     */
    public void checkClasspath(List<String> args, boolean classesOnly, boolean summary) throws IOException {
        SortedSet<String> duplicates = new TreeSet<String>();
        Map<String, List<ClassInfo>> klasses = new HashMap<String, List<ClassInfo>>();
        for (String file : args) {
            ZipFile zf = new ZipFile(file);
            Enumeration entries = zf.entries();

            while (entries.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) entries.nextElement();
                List<ClassInfo> instances = klasses.get(ze.getName());
                if (instances == null) {
                    instances = new ArrayList<ClassInfo>();
                    klasses.put(ze.getName(), instances);
                } else {
                    duplicates.add(ze.getName());
                }
                instances.add(new ClassInfo(file, ze));
            }
            zf.close();
        }

        Set<String> archiveSummary = new HashSet<String>();
        for (String duplicate : duplicates) {
            if (!duplicate.endsWith("/") && (!classesOnly || (classesOnly && duplicate.endsWith(".class")))) {
                if (!summary) {
                    System.out.println(duplicate);
                }
                for (ClassInfo ce : klasses.get(duplicate)) {
                    if (summary) {
                        if (archiveSummary.contains(ce.getLocation())) {
                            continue;
                        }
                        System.out.println(ce.getLocation());
                        archiveSummary.add(ce.getLocation());
                    } else {
                        Date date = new Date(ce.getZe().getTime());
                        System.out.println(String.format("  -> %016x %tF %tR %s", ce.getZe().getCrc(), date, date, ce.getLocation()));
                    }
                }
            }
        }
    }

    public void compareClasspath(List<String> paths) throws IOException {

        List<SortedSet<String>> klassesInArchives = new ArrayList<SortedSet<String>>();

        for (String file : paths) {
            ZipFile zf = new ZipFile(file);
            Enumeration entries = zf.entries();

            SortedSet<String> elements = new TreeSet<String>();

            while (entries.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) entries.nextElement();
                elements.add(ze.getName());
            }
            klassesInArchives.add(elements);
            zf.close();
        }

        compareTwo(paths.get(0), klassesInArchives.get(0), paths.get(1), klassesInArchives.get(1));
    }

    public void compareTwo(String f1, SortedSet<String> s1, String f2, SortedSet<String> s2) {
        Set<String> intersection = new TreeSet<String>(s1);
        intersection.retainAll(s2);

        Set<String> difference1 = new TreeSet<String>(s1);
        difference1.removeAll(s2);

        Set<String> difference2 = new TreeSet<String>(s2);
        difference2.removeAll(s1);

        System.out.println("totals: (" + s1.size() + ", " + s2.size() + ") ");
        System.out.println("intersection: " + intersection.size() + " ");
        for (String path : intersection) {
            System.out.println("  " + path);
        }
        System.out.println("only in " + f1 + ": " + difference1.size());
        for (String path : difference1) {
            System.out.println("  " + path);
        }
        System.out.println("only in " + f2 + ": " + difference2.size());
        for (String path : difference2) {
            System.out.println("  " + path);
        }
    }

    public static void usage() {
        System.err.println("usage: java " + Analyzer.class.getName() + " \\");
        System.err.println(
                "\t--paths file1.jar ... fileN.jar | \\");
        System.err.println(
                "\t--compare file1.jar file2.jar");
        System.exit(
                -1);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            usage();
        }

        if (args[0].equals("--paths")) {
            List<String> paths = new ArrayList<String>();
            boolean classesOnly = false;
            boolean summary = false;
            for (int idx = 1; idx < args.length; idx++) {
                if (args[idx].equals("--classes-only")) {
                    classesOnly = true;
                    continue;
                }
                if (args[idx].equals("--summary")) {
                    summary = true;
                    continue;
                }
                paths.add(args[idx]);
            }
            new Analyzer().checkClasspath(paths, classesOnly, summary);
        } else if (args[0].equals("--compare") && args.length == 3) {
            List<String> paths = new ArrayList<String>();
            for (int idx = 1; idx < args.length; idx++) {
                paths.add(args[idx]);
            }
            new Analyzer().compareClasspath(paths);
        } else {
            usage();
        }
    }
}
