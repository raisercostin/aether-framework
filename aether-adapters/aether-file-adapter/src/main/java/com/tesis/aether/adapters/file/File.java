package com.tesis.aether.adapters.file;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class File extends java.io.File {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1322623669136988710L;

	public File(String pathname) {
		super(pathname.replaceAll("\\", "/").replaceAll("//", "/"));
	}

	public File(String parent, String child) {
		super(parent, child);
	}

	public File(File parent, String child) {
		super(parent, child);
	}

	public File(URI uri) {
		super (uri);
	}

	public String getName() {
		return super.getName();
	}

	public String getParent() {
		return super.getParent();
	}

	public java.io.File getParentFile() {
		return super.getParentFile();
	}

	public String getPath() {
		return super.getPath();
	}

	public boolean isAbsolute() {
		return false;
	}

	public String getAbsolutePath() {
		return null;
	}

	public java.io.File getAbsoluteFile() {
		return null;
	}

	public String getCanonicalPath() throws IOException {
		return super.getCanonicalPath();
	}

	public java.io.File getCanonicalFile() throws IOException {
		return super.getCanonicalFile();
	}

	public URL toURL() throws MalformedURLException {
		return null;
	}

	public URI toURI() {
		return null;
	}

	public boolean canRead() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().canRead(this.getPath());
	}

	public boolean canWrite() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().canWrite(this.getPath());
	}

	public boolean exists() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().exists(this.getPath());
	}

	public boolean isDirectory() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().isDirectory(this.getPath());
	}

	public boolean isFile() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().isFile(this.getPath());
	}

	public boolean isHidden() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().isHidden(this.getPath());
	}

	public long lastModified() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().lastModified(this.getPath());
	}

	public long length() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().length(this.getPath());
	}

	public boolean createNewFile() throws IOException {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().createNewFile(this.getPath());
	}

	public boolean delete() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().delete(this.getPath());
	}

	public void deleteOnExit() {
		com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().delete(this.getPath());
	}

	public String[] list() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().list(this.getPath());
	}

	public String[] list(FilenameFilter filter) {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().canRead(this.getPath(), filter);
	}

	public java.io.File[] listFiles() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().listFiles(this.getPath());
	}

	public java.io.File[] listFiles(FilenameFilter filter) {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().listFiles(this.getPath(), filter);
	}

	public java.io.File[] listFiles(FileFilter filter) {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().listFiles(this.getPath(), filter);
	}

	public boolean mkdir() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().mkdir(this.getPath());
	}

	public boolean mkdirs() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().mkdirs(this.getPath());
	}

	public boolean renameTo(File dest) {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().renameTo(this.getPath(), dest);
	}

	public boolean setLastModified(long time) {
		return false;
	}

	public boolean setReadOnly() {
		return false;
	}

	public boolean setWritable(boolean writable, boolean ownerOnly) {
		return false;
	}

	public boolean setWritable(boolean writable) {
		return false;
	}

	public boolean setReadable(boolean readable, boolean ownerOnly) {
		return false;
	}

	public boolean setReadable(boolean readable) {
		return false;
	}

	public boolean setExecutable(boolean executable, boolean ownerOnly) {
		return false;
	}

	public boolean setExecutable(boolean executable) {
		return false;
	}

	public boolean canExecute() {
		return false;
	}

	public static java.io.File[] listRoots() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().listRoots();
	}

	public long getTotalSpace() {
		return 0;
	}

	public long getFreeSpace() {
		return 0;
	}

	public long getUsableSpace() {
		return 0;
	}

	public int compareTo(File pathname) {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().compareTo(this.getPath(), pathname);
	}

	public boolean equals(Object obj) {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().equals(this.getPath(), obj);
	}

	public int hashCode() {
		return 0;
	}

	public String toString() {
		return com.tesis.aether.adapters.file.FileAetherFrameworkAdapter.getInstance().toString(this.getPath());
	}

}
