package de.amr.maze.demo.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Information about a maze generator or solver algorithm.
 * 
 * @author Armin Reichert
 */
public class Algorithm {

	private final Class<?> algorithmClass;
	private final String description;
	private final String comment;
	private final Object[] tags;

	public Algorithm(Class<?> algorithmClass, String description, String comment, Object... tags) {
		this.algorithmClass = Objects.requireNonNull(algorithmClass);
		this.description = description;
		this.comment = comment;
		this.tags = tags;
	}

	public Class<?> getAlgorithmClass() {
		return algorithmClass;
	}

	public String getDescription() {
		return description;
	}

	public String getComment() {
		return comment;
	}

	public boolean isTagged(Object tag) {
		return Arrays.stream(tags).anyMatch(t -> t.equals(tag));
	}
}