package io.dynamicconfig.kubernetes.watch;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.util.Watch;

/**
 * Abstraction over a Kubernetes ConfigMap watch stream.
 */
public interface ConfigMapWatch extends AutoCloseable {

    boolean hasNext() throws ApiException;

    Watch.Response<V1ConfigMap> next() throws ApiException;

    @Override
    void close();

}
