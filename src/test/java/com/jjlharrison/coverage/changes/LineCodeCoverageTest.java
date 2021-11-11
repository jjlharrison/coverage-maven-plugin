package com.jjlharrison.coverage.changes;

import java.util.Objects;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class LineCodeCoverageTest
{
    @Test
    public void testEquals()
    {
        EqualsVerifier.forClass(LineCodeCoverage.class)
            .withRedefinedSuperclass()
            .withRedefinedSubclass(LineCodeCoverageSubclass.class)
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    private static class LineCodeCoverageSubclass extends LineCodeCoverage
    {
        private final int i;

        public LineCodeCoverageSubclass(final int lineNumber, final int coveredLinesCount, final int missedLinesCount,
                                        final int coveredBranchesCount, final int missedBranchesCount, final int i)
        {
            super(lineNumber, coveredLinesCount, missedLinesCount, coveredBranchesCount, missedBranchesCount);
            this.i = i;
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof LineCodeCoverageSubclass)
            {
                final LineCodeCoverageSubclass that = (LineCodeCoverageSubclass) other;
                return that.canEqual(this) && this.i == that.i && super.equals(that);
            }
            return false;
        }

        public boolean canEqual(final Object other)
        {
            return other instanceof LineCodeCoverageSubclass;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(super.hashCode(), i);
        }
    }
}