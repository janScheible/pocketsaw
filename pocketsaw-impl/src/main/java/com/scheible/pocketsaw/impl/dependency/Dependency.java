package com.scheible.pocketsaw.impl.dependency;

import static com.scheible.pocketsaw.impl.dependency.Dependency.Origin.CODE;
import static com.scheible.pocketsaw.impl.dependency.Dependency.Origin.DESCRIPTOR;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import java.util.Objects;
import com.scheible.pocketsaw.impl.descriptor.PackageGroupDescriptor;
import java.util.EnumSet;

/**
 *
 * @author sj
 */
public class Dependency {

	public enum Origin {
		DESCRIPTOR, CODE
	}

	private final SubModuleDescriptor source;
	private final PackageGroupDescriptor target;

	private final EnumSet<Origin> origins;
	
	private final int codeDependencyCount;

	public Dependency(SubModuleDescriptor source, PackageGroupDescriptor target, boolean isDescriptorDependency, 
			boolean isCodeDependency, int codeDependencyCount) {
		this(source, target, codeDependencyCount, isDescriptorDependency
				? DESCRIPTOR : CODE, isDescriptorDependency && isCodeDependency ? new Origin[]{CODE} : new Origin[0]);

		if (!isCodeDependency && !isDescriptorDependency) {
			throw new IllegalStateException("Dependencies with no origin can't exist!");
		}
	}

	public Dependency(SubModuleDescriptor source, PackageGroupDescriptor target, int codeDependencyCount,
			Origin origin, Origin... additionalOrigins) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(target);

		this.source = source;
		this.target = target;
		
		this.codeDependencyCount = codeDependencyCount;

		this.origins = EnumSet.of(origin, additionalOrigins);
		
		if(!this.origins.contains(Origin.CODE) && codeDependencyCount > 0) {
			throw new IllegalStateException("Descriptor only dependencies with code dependency count > 0 do not make sense at all!");
		}
	}

	public SubModuleDescriptor getSource() {
		return this.source;
	}

	public PackageGroupDescriptor getTarget() {
		return this.target;
	}

	public int getCodeDependencyCount() {
		return codeDependencyCount;
	}

	public EnumSet<Origin> getOrigins() {
		return origins;
	}

	public boolean hasCodeOrigin() {
		return origins.contains(CODE);
	}

	public boolean hasDescriptorOrigin() {
		return origins.contains(DESCRIPTOR);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.source, this.target, this.origins);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj != null && obj.getClass().equals(this.getClass())) {
			Dependency other = (Dependency) obj;
			return this.source.equals(other.source) && this.target.equals(other.target) 
					&& this.codeDependencyCount == other.codeDependencyCount && this.origins.equals(other.origins);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + String.join(", ",
				"source = " + this.source.getName(),
				"target = " + this.target.getName(),
				"codeDependencyCount = " + this.codeDependencyCount,
				"origins = " + this.origins
		) + "]";
	}
}
