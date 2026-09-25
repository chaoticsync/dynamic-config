package io.dynamicconfig.kubernetes.watch;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ConfigMap;

/**
 * Abstraction over the Kubernetes ConfigMap API for testability.
 */
public interface ConfigMapApiClient {

    V1ConfigMap getConfigMap(String namespace, String name) throws ApiException;

    ConfigMapWatch openWatch(String namespace, String name, String resourceVersion) throws ApiException;

}
