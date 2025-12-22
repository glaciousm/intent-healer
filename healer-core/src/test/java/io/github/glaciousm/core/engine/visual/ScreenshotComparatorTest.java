package io.github.glaciousm.core.engine.visual;

import org.junit.jupiter.api.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ScreenshotComparator.
 */
@DisplayName("ScreenshotComparator")
class ScreenshotComparatorTest {

    private ScreenshotComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new ScreenshotComparator();
    }

    @Nested
    @DisplayName("Identical Images")
    class IdenticalImagesTests {

        @Test
        @DisplayName("should pass for identical images")
        void shouldPassForIdenticalImages() {
            BufferedImage img = createSolidColorImage(100, 100, Color.BLUE);

            ScreenshotComparator.ComparisonResult result = comparator.compare(img, img);

            assertThat(result.passed()).isTrue();
            assertThat(result.similarity()).isEqualTo(1.0);
            assertThat(result.getDifferenceCount()).isZero();
            assertThat(result.hasError()).isFalse();
        }

        @Test
        @DisplayName("should pass for identical solid color images")
        void shouldPassForIdenticalSolidColorImages() {
            BufferedImage img1 = createSolidColorImage(100, 100, Color.RED);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.RED);

            ScreenshotComparator.ComparisonResult result = comparator.compare(img1, img2);

            assertThat(result.passed()).isTrue();
            assertThat(result.similarity()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("Different Images")
    class DifferentImagesTests {

        @Test
        @DisplayName("should fail for completely different images")
        void shouldFailForCompletelyDifferentImages() {
            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.BLACK);

            ScreenshotComparator.ComparisonResult result = comparator.compare(img1, img2);

            assertThat(result.passed()).isFalse();
            assertThat(result.similarity()).isLessThan(0.95);
            assertThat(result.getDifferenceCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should detect partial differences")
        void shouldDetectPartialDifferences() {
            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.WHITE);

            // Add a red square to img2
            Graphics2D g = img2.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(25, 25, 50, 50);
            g.dispose();

            ScreenshotComparator.ComparisonResult result = comparator.compare(img1, img2);

            assertThat(result.passed()).isFalse();
            assertThat(result.similarity()).isGreaterThan(0).isLessThan(1);
            assertThat(result.getDifferenceCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should calculate correct total difference pixels")
        void shouldCalculateCorrectTotalDifferencePixels() {
            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.WHITE);

            // Add a small difference
            Graphics2D g = img2.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 20, 20);
            g.dispose();

            ScreenshotComparator.ComparisonResult result = comparator.compare(img1, img2);

            assertThat(result.getTotalDifferencePixels()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Dimension Mismatch")
    class DimensionMismatchTests {

        @Test
        @DisplayName("should fail for different dimensions")
        void shouldFailForDifferentDimensions() {
            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(200, 200, Color.WHITE);

            ScreenshotComparator.ComparisonResult result = comparator.compare(img1, img2);

            assertThat(result.passed()).isFalse();
            assertThat(result.hasError()).isTrue();
            assertThat(result.errorMessage()).contains("Dimension mismatch");
        }

        @Test
        @DisplayName("should include dimensions in error message")
        void shouldIncludeDimensionsInErrorMessage() {
            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(150, 120, Color.WHITE);

            ScreenshotComparator.ComparisonResult result = comparator.compare(img1, img2);

            assertThat(result.errorMessage()).contains("100").contains("150");
        }
    }

    @Nested
    @DisplayName("Null Handling")
    class NullHandlingTests {

        @Test
        @DisplayName("should handle null baseline")
        void shouldHandleNullBaseline() {
            BufferedImage img = createSolidColorImage(100, 100, Color.WHITE);

            ScreenshotComparator.ComparisonResult result = comparator.compare(
                    (BufferedImage) null, img);

            assertThat(result.passed()).isFalse();
            assertThat(result.hasError()).isTrue();
            assertThat(result.errorMessage()).contains("null");
        }

        @Test
        @DisplayName("should handle null actual")
        void shouldHandleNullActual() {
            BufferedImage img = createSolidColorImage(100, 100, Color.WHITE);

            ScreenshotComparator.ComparisonResult result = comparator.compare(
                    img, (BufferedImage) null);

            assertThat(result.passed()).isFalse();
            assertThat(result.hasError()).isTrue();
        }

        @Test
        @DisplayName("should handle both null")
        void shouldHandleBothNull() {
            ScreenshotComparator.ComparisonResult result = comparator.compare(
                    (BufferedImage) null, (BufferedImage) null);

            assertThat(result.passed()).isFalse();
            assertThat(result.hasError()).isTrue();
        }
    }

    @Nested
    @DisplayName("Region Comparison")
    class RegionComparisonTests {

        @Test
        @DisplayName("should compare only specified region")
        void shouldCompareOnlySpecifiedRegion() {
            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.WHITE);

            // Add difference outside region of interest
            Graphics2D g = img2.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 20, 20); // Top-left corner
            g.dispose();

            // Compare only bottom-right region (which is unchanged)
            Rectangle region = new Rectangle(60, 60, 30, 30);
            ScreenshotComparator.ComparisonResult result = comparator.compareRegion(img1, img2, region);

            assertThat(result.passed()).isTrue();
        }

        @Test
        @DisplayName("should detect differences in region")
        void shouldDetectDifferencesInRegion() {
            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.WHITE);

            // Add difference inside region of interest
            Graphics2D g = img2.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(30, 30, 40, 40);
            g.dispose();

            Rectangle region = new Rectangle(25, 25, 50, 50);
            ScreenshotComparator.ComparisonResult result = comparator.compareRegion(img1, img2, region);

            assertThat(result.passed()).isFalse();
        }

        @Test
        @DisplayName("should error on out of bounds region")
        void shouldErrorOnOutOfBoundsRegion() {
            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.WHITE);

            Rectangle region = new Rectangle(80, 80, 50, 50); // Extends beyond image
            ScreenshotComparator.ComparisonResult result = comparator.compareRegion(img1, img2, region);

            assertThat(result.hasError()).isTrue();
            assertThat(result.errorMessage()).contains("out of bounds");
        }
    }

    @Nested
    @DisplayName("Ignore Regions")
    class IgnoreRegionsTests {

        @Test
        @DisplayName("should pass when differences are in ignored regions")
        void shouldPassWhenDifferencesAreInIgnoredRegions() {
            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.WHITE);

            // Add difference that will be ignored
            Graphics2D g = img2.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(10, 10, 20, 20);
            g.dispose();

            List<Rectangle> ignoreRegions = List.of(new Rectangle(5, 5, 30, 30));
            ScreenshotComparator.ComparisonResult result = comparator.compareIgnoringRegions(img1, img2, ignoreRegions);

            assertThat(result.passed()).isTrue();
        }

        @Test
        @DisplayName("should fail when differences are outside ignored regions")
        void shouldFailWhenDifferencesAreOutsideIgnoredRegions() {
            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.WHITE);

            // Add difference outside ignored region
            Graphics2D g = img2.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(70, 70, 20, 20);
            g.dispose();

            List<Rectangle> ignoreRegions = List.of(new Rectangle(0, 0, 30, 30));
            ScreenshotComparator.ComparisonResult result = comparator.compareIgnoringRegions(img1, img2, ignoreRegions);

            assertThat(result.passed()).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder Configuration")
    class BuilderTests {

        @Test
        @DisplayName("should configure custom similarity threshold")
        void shouldConfigureCustomSimilarityThreshold() {
            ScreenshotComparator custom = ScreenshotComparator.builder()
                    .similarityThreshold(0.5)
                    .build();

            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createHalfAndHalfImage(100, 100, Color.WHITE, Color.BLACK);

            // With default threshold (0.95), this would fail
            // With 0.5 threshold, it should pass at 50% similarity
            ScreenshotComparator.ComparisonResult result = custom.compare(img1, img2);

            // The similarity is around 0.5, so it should pass with low threshold
            assertThat(result.similarity()).isCloseTo(0.5, org.assertj.core.data.Offset.offset(0.1));
        }

        @Test
        @DisplayName("should configure custom block size")
        void shouldConfigureCustomBlockSize() {
            ScreenshotComparator custom = ScreenshotComparator.builder()
                    .blockSize(5)
                    .build();

            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.WHITE);

            ScreenshotComparator.ComparisonResult result = custom.compare(img1, img2);

            assertThat(result.passed()).isTrue();
        }

        @Test
        @DisplayName("should disable diff image generation")
        void shouldDisableDiffImageGeneration() {
            ScreenshotComparator custom = ScreenshotComparator.builder()
                    .generateDiffImage(false)
                    .build();

            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.BLACK);

            ScreenshotComparator.ComparisonResult result = custom.compare(img1, img2);

            assertThat(result.diffImage()).isNull();
        }

        @Test
        @DisplayName("should generate diff image when enabled")
        void shouldGenerateDiffImageWhenEnabled() {
            ScreenshotComparator custom = ScreenshotComparator.builder()
                    .generateDiffImage(true)
                    .build();

            BufferedImage img1 = createSolidColorImage(100, 100, Color.WHITE);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.BLACK);

            ScreenshotComparator.ComparisonResult result = custom.compare(img1, img2);

            assertThat(result.diffImage()).isNotNull();
            assertThat(result.getDiffImageBase64()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("ComparisonResult")
    class ComparisonResultTests {

        @Test
        @DisplayName("should create error result")
        void shouldCreateErrorResult() {
            ScreenshotComparator.ComparisonResult result = ScreenshotComparator.ComparisonResult.error("Test error");

            assertThat(result.passed()).isFalse();
            assertThat(result.hasError()).isTrue();
            assertThat(result.errorMessage()).isEqualTo("Test error");
            assertThat(result.similarity()).isZero();
        }

        @Test
        @DisplayName("should create dimension mismatch result")
        void shouldCreateDimensionMismatchResult() {
            ScreenshotComparator.ComparisonResult result =
                    ScreenshotComparator.ComparisonResult.dimensionMismatch(100, 200, 150, 250);

            assertThat(result.passed()).isFalse();
            assertThat(result.hasError()).isTrue();
            assertThat(result.errorMessage()).contains("100").contains("200").contains("150").contains("250");
        }
    }

    @Nested
    @DisplayName("DifferenceRegion")
    class DifferenceRegionTests {

        @Test
        @DisplayName("should convert to Rectangle")
        void shouldConvertToRectangle() {
            ScreenshotComparator.DifferenceRegion region =
                    new ScreenshotComparator.DifferenceRegion(10, 20, 30, 40, 100);

            Rectangle rect = region.toRectangle();

            assertThat(rect.x).isEqualTo(10);
            assertThat(rect.y).isEqualTo(20);
            assertThat(rect.width).isEqualTo(30);
            assertThat(rect.height).isEqualTo(40);
        }
    }

    @Nested
    @DisplayName("Byte Array Comparison")
    class ByteArrayComparisonTests {

        @Test
        @DisplayName("should compare byte arrays")
        void shouldCompareByteArrays() throws IOException {
            BufferedImage img1 = createSolidColorImage(100, 100, Color.BLUE);
            BufferedImage img2 = createSolidColorImage(100, 100, Color.BLUE);

            byte[] bytes1 = imageToBytes(img1);
            byte[] bytes2 = imageToBytes(img2);

            ScreenshotComparator.ComparisonResult result = comparator.compare(bytes1, bytes2);

            assertThat(result.passed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Anti-aliasing Tolerance")
    class AntiAliasingTests {

        @Test
        @DisplayName("should tolerate minor pixel differences")
        void shouldTolerateMinorPixelDifferences() {
            BufferedImage img1 = createSolidColorImage(100, 100, new Color(100, 100, 100));
            BufferedImage img2 = createSolidColorImage(100, 100, new Color(105, 105, 105)); // Slight difference

            ScreenshotComparator.ComparisonResult result = comparator.compare(img1, img2);

            assertThat(result.passed()).isTrue();
        }
    }

    // Helper methods

    private BufferedImage createSolidColorImage(int width, int height, Color color) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }

    private BufferedImage createHalfAndHalfImage(int width, int height, Color left, Color right) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(left);
        g.fillRect(0, 0, width / 2, height);
        g.setColor(right);
        g.fillRect(width / 2, 0, width / 2, height);
        g.dispose();
        return img;
    }

    private byte[] imageToBytes(BufferedImage image) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }
}
