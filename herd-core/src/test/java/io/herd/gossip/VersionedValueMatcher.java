package io.herd.gossip;
import static org.mockito.Mockito.*;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class VersionedValueMatcher extends TypeSafeMatcher<VersionedValue> {
    
    private final String value;
    
    private VersionedValueMatcher(String value) {
        this.value = value;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("value is not equal to ").appendValue(value);
    }

    @Override
    protected boolean matchesSafely(VersionedValue item) {
        return value.equals(item.getValue());
    }
    
    public static final VersionedValueMatcher isValue(String value) {
        return new VersionedValueMatcher(value);
    }
    
    public static final VersionedValue eqVersionedValue(String value) {
        return argThat(isValue(value));
    }
    
}