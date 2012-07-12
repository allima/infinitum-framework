package com.clarionmedia.infinitum.reflection.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.clarionmedia.infinitum.reflection.PackageReflector;

/**
 * The class and subpackage contents of a package.
 * 
 * <p>
 * Adapted from android.test.ClassPathPackageInfo.
 */
public class Package {

	private final PackageReflector source;
	private final Set<String> subpackageNames;
	private final Set<Class<?>> topLevelClasses;

	Package(PackageReflector source, Set<String> subpackageNames,
			Set<Class<?>> topLevelClasses) {
		this.source = source;
		this.subpackageNames = Collections.unmodifiableSet(subpackageNames);
		this.topLevelClasses = Collections.unmodifiableSet(topLevelClasses);
	}

	public Set<Class<?>> getTopLevelClassesRecursive() {
		Set<Class<?>> set = new TreeSet<Class<?>>(
				PackageReflector.ORDER_CLASS_BY_NAME);
		addTopLevelClassesTo(set);
		return set;
	}

	private Set<Package> getSubpackages() {
		Set<Package> info = new HashSet<Package>();
		for (String name : subpackageNames) {
			info.add(source.scan(name));
		}
		return info;
	}

	private void addTopLevelClassesTo(Set<Class<?>> set) {
		set.addAll(topLevelClasses);
		for (Package info : getSubpackages()) {
			info.addTopLevelClassesTo(set);
		}
	}
}