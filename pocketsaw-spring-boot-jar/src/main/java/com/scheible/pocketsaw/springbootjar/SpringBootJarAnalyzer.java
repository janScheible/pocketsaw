package com.scheible.pocketsaw.springbootjar;

import com.scheible.pocketsaw.impl.code.DependencyFilter;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.PackageDependencySource;
import com.scheible.pocketsaw.impl.code.jdeps.JdepsWrapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 *
 * @author sj
 */
public class SpringBootJarAnalyzer implements PackageDependencySource {

	static final String SPRING_BOOT_ROOT = "BOOT-INF/";
	static final String CLASSES_ROOT = SPRING_BOOT_ROOT + "classes/";
	static final String LIBS_ROOT = SPRING_BOOT_ROOT + "lib/";
	static final String ZIP_SEPARATOR = "/";
	
	@Override
	public PackageDependencies read(final File springBootJarFile) {
		throw new IllegalStateException("'root-packages' is a mandatory parameter!");
	}

	@Override
	public PackageDependencies read(final File springBootJarFile, final Set<Entry<String, String>> parameters) {
		final Set<String> rootPackages = new HashSet<>();
		boolean keepTempDirContents = false;
		String tempDirName = null;
		
		for(final Entry<String, String> parameter : parameters) {
			final String key = parameter.getKey().toLowerCase().trim();
			if("root-packages".equals(key)) {
				rootPackages.addAll(new HashSet<>(Arrays.asList(parameter.getValue().trim().split(Pattern.quote(",")))));
			} else if("keep-temp-dir-contents".equals(key)) {
				keepTempDirContents = Boolean.parseBoolean(parameter.getValue().toLowerCase().trim());
			} else if("temp-dir-name".equals(key)) {
				tempDirName = parameter.getValue().trim();
			}
		}
	
		if(rootPackages.isEmpty()) {
			throw new IllegalStateException("'root-packages' is a required parameter!");
		}
		
		return readInternal(springBootJarFile, rootPackages, keepTempDirContents, Optional.ofNullable(tempDirName));
	}

	PackageDependencies readInternal(final File springBootJarFile, final Set<String> rootPackages, 
			final boolean keepTempDirContents, final Optional<String> tempDirName) {
		final Set<String> classPrefixes = rootPackages.stream()
				.map(p -> p.replaceAll(Pattern.quote("."), ZIP_SEPARATOR)).collect(Collectors.toSet());
		final File tempDir = new File(System.getProperty("java.io.tmpdir"),
				tempDirName.orElseGet(() -> UUID.randomUUID().toString().replaceAll(Pattern.quote("-"), "")));
		tempDir.mkdir();
		System.out.println("temp dir: " + tempDir + " (" + (keepTempDirContents ? "won't" : "will") + " be deleted)");

		try (final ZipFile zipFile = new ZipFile(springBootJarFile)) {
			final Set<ZipEntry> jarZipEntries = new HashSet<>();

			final Enumeration<? extends ZipEntry> entryEnumeration = zipFile.entries();
			while (entryEnumeration.hasMoreElements()) {
				final ZipEntry springBootJarEntry = entryEnumeration.nextElement();

				if (springBootJarEntry.getName().startsWith(LIBS_ROOT) && springBootJarEntry.getName().endsWith(".jar")) {
					try (final ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream(springBootJarEntry))) {
						ZipEntry jarEntry;
						while ((jarEntry = zipInputStream.getNextEntry()) != null) {
							final String jarName = jarEntry.getName();
							if (classPrefixes.stream().anyMatch(p -> jarName.startsWith(p))) {
								jarZipEntries.add(springBootJarEntry);
								break;
							}
						}
					}
				} else if (springBootJarEntry.getName().startsWith(CLASSES_ROOT)) {
					write(zipFile.getInputStream(springBootJarEntry), tempDir,
							springBootJarEntry.getName().substring(SPRING_BOOT_ROOT.length()), springBootJarEntry.isDirectory());
				}
			}

			for (final ZipEntry jarZipEntry : jarZipEntries) {
				write(zipFile.getInputStream(jarZipEntry), tempDir,
						jarZipEntry.getName().substring(LIBS_ROOT.length()), jarZipEntry.isDirectory());
			}
			
			final PackageDependencies result = JdepsWrapper.run("classes", Optional.of(tempDir), Optional.of("*.jar"), 
					new DependencyFilter(rootPackages, new HashSet<>(), true));

			if(!keepTempDirContents) {
				deleteRecursively(tempDir);
			}

			return result;
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	static void write(final InputStream inputStream, final File rootDir, final String path, final boolean isDirectory) {
		try (final InputStream source = inputStream) {
			final File targetFile = new File(rootDir, path);
			final boolean skip = targetFile.exists();
			if (!skip) {
				if (isDirectory) {
					targetFile.mkdirs();
				} else {
					Files.copy(source, targetFile.toPath());
				}
			}
			System.out.println(targetFile + (skip ? " (skipped)" : ""));			
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
	
	static void deleteRecursively(File dir) throws IOException {
		Files.walk(dir.toPath())
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
	}

	@Override
	public String getIdentifier() {
		return "spring-boot-jar";
	}
}
