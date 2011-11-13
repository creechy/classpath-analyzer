package org.fakebelieve.classpath.analyzer;


import java.util.zip.ZipEntry;


public class ClassInfo {

    public String location;
    public ZipEntry ze;

    public ClassInfo(String location, ZipEntry ze) {
        this.location = location;
        this.ze = ze;
    }

    public String getLocation() {
        return location;
    }

    public ZipEntry getZe() {
        return ze;
    }


}
