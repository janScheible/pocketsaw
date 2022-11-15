package com.scheible.pocketsaw.es6modules;

import com.scheible.es2020parser.util.ListeningJavaScriptParser;
import com.scheible.es2020parser.util.module.ImportParseListener;
import com.scheible.pocketsaw.es6modules.BundleReporter.BundleReport;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.PackageDependency;
import com.scheible.pocketsaw.impl.code.PackageDependencySource;
import com.scheible.pocketsaw.impl.code.TypeDependency;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import static java.util.Collections.emptySet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author sj
 */
public class Es6ModulesSource implements PackageDependencySource {
	
	public static class ParameterBuilder {
		
		private static final String PRINT_BUNDLE_REPORT = "print-bundle-report";
		private static final String START_MODULE = "start-module";
		
		public static Set<Entry<String, String>> printBundleReport(Set<Entry<String, String>> parameters) {
			parameters.add(new SimpleImmutableEntry<>(PRINT_BUNDLE_REPORT, "true"));
			return parameters;
		}
		
		public static Set<Entry<String, String>> startModule(Set<Entry<String, String>> parameters, String startModule) {
			parameters.add(new SimpleImmutableEntry<>(START_MODULE, startModule));
			return parameters;
		}
	}	

	static class JavaScriptFile {

		private final Path file;
		private final String source;

		public JavaScriptFile(final Path file, final String source) {
			this.file = file;
			this.source = source;
		}

		public Path getFile() {
			return file;
		}

		public String getSource() {
			return source;
		}
	}

	static class ImportPath {

		private final Path path;
		private final boolean dynamic;

		ImportPath(Path path, boolean dynamic) {
			this.path = path;
			this.dynamic = dynamic;
		}

		Path getPath() {
			return path;
		}

		boolean isDynamic() {
			return dynamic;
		}

		@Override
		public int hashCode() {
			return Objects.hash(path, dynamic);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (obj != null && getClass().equals(obj.getClass())) {
				ImportPath other = (ImportPath) obj;
				return Objects.equals(path, other.path) && Objects.equals(dynamic, other.dynamic);
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return (dynamic ? "dynamic " : "") + path;
		}
	}
	
	private final Function<Path, Stream<JavaScriptFile>> javaScriptFileWalker;

	private BundleReport bundleReport;

	public Es6ModulesSource() {
		javaScriptFileWalker = sourceRootPath -> {
			try {
				return Files.walk(sourceRootPath)
						.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".js"))
						.map(p -> {
							try {
								return new JavaScriptFile(p, new String(Files.readAllBytes(p), UTF_8));
							} catch (IOException ex) {
								throw new UncheckedIOException(ex);
							}
						});
			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		};
	}

	Es6ModulesSource(final Function<Path, Stream<JavaScriptFile>> javaScriptFileWalker) {
		this.javaScriptFileWalker = javaScriptFileWalker;
	}

	@Override
	public PackageDependencies read(final File sourceRootDir) {
		return read(sourceRootDir, emptySet());
	}

	@Override
	public PackageDependencies read(final File sourceRootDir, final Set<Entry<String, String>> parameters) {
		boolean printBundleReport = false;
		Optional<String> startModule = Optional.empty();
		
		for(final Entry<String, String> parameter : parameters) {
			final String key = parameter.getKey().toLowerCase().trim();
			if(ParameterBuilder.PRINT_BUNDLE_REPORT.equals(key)) {
				printBundleReport =  Boolean.parseBoolean(parameter.getValue().toLowerCase().trim());
			} else if(ParameterBuilder.START_MODULE.equals(key)) {
				startModule = Optional.ofNullable(parameter.getValue());
			}
		}
		
		final Path sourceRootPath = toCanonicalPath(sourceRootDir);

		final Map<Path, Set<ImportPath>> moduleDependencies = javaScriptFileWalker.apply(sourceRootPath)
				.map(jsf -> {
					final Path currentModulePath = jsf.getFile()
							.subpath(sourceRootPath.getNameCount() - 1, jsf.getFile().getNameCount())
							.resolveSibling(jsf.getFile().getFileName().toString()
									.substring(0, jsf.getFile().getFileName().toString().lastIndexOf(".")));

					final Set<ImportPath> importModulePaths = ListeningJavaScriptParser.parse(jsf.getSource(),
							new ImportParseListener()).getModuleSpecifiers().stream()
							.filter(ms -> ms.isRelative() || ms.isSourceRootRelative())
							.map(ms -> new ImportPath(ms.resolve(sourceRootPath, jsf.getFile()), ms.isDynamic()))
							.map(p -> new ImportPath(p.path.subpath(sourceRootPath.getNameCount() - 1, p.path.getNameCount()), p.dynamic))
							.collect(Collectors.toSet());

					return new SimpleImmutableEntry<>(currentModulePath, importModulePaths);
				})
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		bundleReport = BundleReporter.create(moduleDependencies, startModule);
		if(printBundleReport) {
			System.out.println(bundleReport);
		}

		return toPackageDependencies(sourceRootDir.getName(), moduleDependencies);
	}

	public BundleReport getBundleReport() {
		if (bundleReport == null) {
			throw new IllegalStateException("Report not yet available, the method read(File) has to be called first.");
		}

		return bundleReport;
	}

	@Override
	public String getIdentifier() {
		return "es6-modules";
	}

	PackageDependencies toPackageDependencies(final String rootNamespacePart, final Map<Path, Set<ImportPath>> moduleDependencies) {
		final Map<PackageDependency, Set<TypeDependency>> moduleLevelNamespaceDependencies = new HashMap<>();
		final Map<String, Set<String>> namespaceModules = new HashMap<>();

		for (final Entry<Path, Set<ImportPath>> moduleDependency : moduleDependencies.entrySet()) {
			final String from = toNamespaceNotation(moduleDependency.getKey());
			final String fromNamepsace = toNamespaceNotation(moduleDependency.getKey().getParent());

			namespaceModules.computeIfAbsent(fromNamepsace, key -> new HashSet<>()).add(from);

			for (final ImportPath toPath : moduleDependency.getValue()) {
				final String to = toNamespaceNotation(toPath.path);
				final String toNamespace = toNamespaceNotation(toPath.path.getParent());

				namespaceModules.computeIfAbsent(toNamespace, key -> new HashSet<>()).add(to);

				if (!fromNamepsace.equals(toNamespace)) {
					moduleLevelNamespaceDependencies.computeIfAbsent(new PackageDependency(fromNamepsace,
							toNamespace), key -> new HashSet<>())
							.add(new TypeDependency(fromNamepsace, from, toNamespace, to));
				}
			}
		}

		return PackageDependencies.withClassLevelDependencies(moduleLevelNamespaceDependencies, namespaceModules);
	}

	static String toNamespaceNotation(final Path path) {
		return path.toString().replaceAll(Pattern.quote("\\"), ".").replaceAll(Pattern.quote("/"), ".");
	}
	
	private Path toCanonicalPath(final File file) {
		try {
			return file.getCanonicalFile().toPath();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
