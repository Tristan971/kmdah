package moe.tristan.kmdah.operator.model.vacuum;

import java.util.List;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;
import org.springframework.util.unit.DataSize;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractBucketScanResult {

    @Parameter
    public abstract DataSize getSize();

    @Parameter
    public abstract List<S3ObjectSummary> getObjects();

}
