package org.cryptomator.cryptofs;

import org.cryptomator.cryptofs.CryptoPathMapper.CiphertextFileType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemLoopException;
import java.nio.file.Path;

public class SymlinksTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private final CryptoPathMapper cryptoPathMapper = Mockito.mock(CryptoPathMapper.class);
	private final OpenCryptoFiles openCryptoFiles = Mockito.mock(OpenCryptoFiles.class);
	private final ReadonlyFlag readonlyFlag = Mockito.mock(ReadonlyFlag.class);

	private final CryptoPath cleartextPath = Mockito.mock(CryptoPath.class, "cleartextPath");
	private final OpenCryptoFile ciphertextFile = Mockito.mock(OpenCryptoFile.class);
	private final Path ciphertextPath = Mockito.mock(Path.class, "ciphertextPath");

	private Symlinks inTest;

	@Before
	public void setup() throws IOException {
		inTest = new Symlinks(cryptoPathMapper, openCryptoFiles, readonlyFlag);

		Mockito.when(openCryptoFiles.getOrCreate(Mockito.eq(ciphertextPath), Mockito.any())).thenReturn(ciphertextFile);
	}

	@Test
	public void testCreateSymbolicLink() throws IOException {
		Path target = Mockito.mock(Path.class, "targetPath");
		Mockito.doNothing().when(cryptoPathMapper).assertNonExisting(cleartextPath);
		Mockito.when(cryptoPathMapper.getCiphertextFilePath(cleartextPath, CiphertextFileType.SYMLINK)).thenReturn(ciphertextPath);
		Mockito.when(target.toString()).thenReturn("/symlink/target/path");

		inTest.createSymbolicLink(cleartextPath, target, null);

		ArgumentCaptor<ByteBuffer> bytesWritten = ArgumentCaptor.forClass(ByteBuffer.class);
		Mockito.verify(openCryptoFiles).writeCiphertextFile(Mockito.eq(ciphertextPath), Mockito.any(), bytesWritten.capture());
		Assert.assertEquals("/symlink/target/path", StandardCharsets.UTF_8.decode(bytesWritten.getValue()).toString());
	}

	@Test
	public void testReadSymbolicLink() throws IOException {
		String targetPath = "/symlink/target/path2";
		CryptoPath resolvedTargetPath = Mockito.mock(CryptoPath.class, "resolvedTargetPath");
		Mockito.when(cryptoPathMapper.getCiphertextFilePath(cleartextPath, CiphertextFileType.SYMLINK)).thenReturn(ciphertextPath);
		Mockito.when(openCryptoFiles.readCiphertextFile(Mockito.eq(ciphertextPath), Mockito.any(), Mockito.anyInt())).thenReturn(StandardCharsets.UTF_8.encode(targetPath));

		Mockito.when(cleartextPath.resolveSibling(targetPath)).thenReturn(resolvedTargetPath);

		CryptoPath read = inTest.readSymbolicLink(cleartextPath);

		Assert.assertSame(resolvedTargetPath, read);
	}

	@Test
	public void testResolveRecursivelyForRegularFile() throws IOException {
		CryptoPath cleartextPath1 = Mockito.mock(CryptoPath.class);
		Mockito.when(cryptoPathMapper.getCiphertextFileType(cleartextPath1)).thenReturn(CiphertextFileType.FILE);

		CryptoPath resolved = inTest.resolveRecursively(cleartextPath1);

		Assert.assertSame(cleartextPath1, resolved);
	}

	@Test
	public void testResolveRecursively() throws IOException {
		CryptoPath cleartextPath1 = Mockito.mock(CryptoPath.class);
		CryptoPath cleartextPath2 = Mockito.mock(CryptoPath.class);
		CryptoPath cleartextPath3 = Mockito.mock(CryptoPath.class);
		Path ciphertextPath1 = Mockito.mock(Path.class);
		Path ciphertextPath2 = Mockito.mock(Path.class);
		Mockito.when(cryptoPathMapper.getCiphertextFileType(cleartextPath1)).thenReturn(CiphertextFileType.SYMLINK);
		Mockito.when(cryptoPathMapper.getCiphertextFileType(cleartextPath2)).thenReturn(CiphertextFileType.SYMLINK);
		Mockito.when(cryptoPathMapper.getCiphertextFileType(cleartextPath3)).thenReturn(CiphertextFileType.FILE);
		Mockito.when(cryptoPathMapper.getCiphertextFilePath(cleartextPath1, CiphertextFileType.SYMLINK)).thenReturn(ciphertextPath1);
		Mockito.when(cryptoPathMapper.getCiphertextFilePath(cleartextPath2, CiphertextFileType.SYMLINK)).thenReturn(ciphertextPath2);
		Mockito.when(openCryptoFiles.readCiphertextFile(Mockito.eq(ciphertextPath1), Mockito.any(), Mockito.anyInt())).thenReturn(StandardCharsets.UTF_8.encode("file2"));
		Mockito.when(openCryptoFiles.readCiphertextFile(Mockito.eq(ciphertextPath2), Mockito.any(), Mockito.anyInt())).thenReturn(StandardCharsets.UTF_8.encode("file3"));
		Mockito.when(cleartextPath1.resolveSibling("file2")).thenReturn(cleartextPath2);
		Mockito.when(cleartextPath2.resolveSibling("file3")).thenReturn(cleartextPath3);

		CryptoPath resolved = inTest.resolveRecursively(cleartextPath1);

		Assert.assertSame(cleartextPath3, resolved);
	}

	@Test
	public void testResolveRecursivelyWithLoop() throws IOException {
		CryptoPath cleartextPath1 = Mockito.mock(CryptoPath.class);
		CryptoPath cleartextPath2 = Mockito.mock(CryptoPath.class);
		CryptoPath cleartextPath3 = Mockito.mock(CryptoPath.class);
		Path ciphertextPath1 = Mockito.mock(Path.class);
		Path ciphertextPath2 = Mockito.mock(Path.class);
		Path ciphertextPath3 = Mockito.mock(Path.class);
		Mockito.when(cryptoPathMapper.getCiphertextFileType(cleartextPath1)).thenReturn(CiphertextFileType.SYMLINK);
		Mockito.when(cryptoPathMapper.getCiphertextFileType(cleartextPath2)).thenReturn(CiphertextFileType.SYMLINK);
		Mockito.when(cryptoPathMapper.getCiphertextFileType(cleartextPath3)).thenReturn(CiphertextFileType.SYMLINK);
		Mockito.when(cryptoPathMapper.getCiphertextFilePath(cleartextPath1, CiphertextFileType.SYMLINK)).thenReturn(ciphertextPath1);
		Mockito.when(cryptoPathMapper.getCiphertextFilePath(cleartextPath2, CiphertextFileType.SYMLINK)).thenReturn(ciphertextPath2);
		Mockito.when(cryptoPathMapper.getCiphertextFilePath(cleartextPath3, CiphertextFileType.SYMLINK)).thenReturn(ciphertextPath3);
		Mockito.when(openCryptoFiles.readCiphertextFile(Mockito.eq(ciphertextPath1), Mockito.any(), Mockito.anyInt())).thenReturn(StandardCharsets.UTF_8.encode("file2"));
		Mockito.when(openCryptoFiles.readCiphertextFile(Mockito.eq(ciphertextPath2), Mockito.any(), Mockito.anyInt())).thenReturn(StandardCharsets.UTF_8.encode("file3"));
		Mockito.when(openCryptoFiles.readCiphertextFile(Mockito.eq(ciphertextPath3), Mockito.any(), Mockito.anyInt())).thenReturn(StandardCharsets.UTF_8.encode("file1"));
		Mockito.when(cleartextPath1.resolveSibling("file2")).thenReturn(cleartextPath2);
		Mockito.when(cleartextPath2.resolveSibling("file3")).thenReturn(cleartextPath3);
		Mockito.when(cleartextPath3.resolveSibling("file1")).thenReturn(cleartextPath1);

		thrown.expect(FileSystemLoopException.class);
		inTest.resolveRecursively(cleartextPath1);
	}

}
