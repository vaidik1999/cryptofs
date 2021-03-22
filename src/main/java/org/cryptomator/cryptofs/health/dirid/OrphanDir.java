package org.cryptomator.cryptofs.health.dirid;

import org.cryptomator.cryptofs.health.api.DiagnosticResult;

import java.nio.file.Path;

/**
 * An orphan directory is a detached node, not referenced by any dir.c9r file.
 */
public class OrphanDir implements DiagnosticResult {

	private final Path dir;

	OrphanDir(Path dir) {
		this.dir = dir;
	}

	@Override
	public Severity getServerity() {
		return Severity.WARN;
	}

	@Override
	public String description() {
		return String.format("Orphan directory: %s", dir);
	}

	// fix: create new dirId inside of L+F dir and rename existing dir accordingly.
}
