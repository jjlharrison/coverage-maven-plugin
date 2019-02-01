/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
import org.junit.Test;

public class FooTest
{
    @Test
    public void testThisMethodIsCovered()
    {
        assert new Foo().thisMethodIsCovered("a").equals("b");
    }
}