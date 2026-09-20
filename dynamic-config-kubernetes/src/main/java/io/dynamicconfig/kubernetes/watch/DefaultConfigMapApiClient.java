package io.dynamicconfig.kubernetes.watch;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Watch;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DefaultConfigMapApiClient implements ConfigMapApiClient {

    private final ApiClient apiClient;
    private final CoreV1Api coreV1Api;

    public DefaultConfigMapApiClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient cannot be null");
        this.coreV1Api = new CoreV1Api(apiClient);
    }

    public static DefaultConfigMapApiClient fromDefaultClient() throws IOException {
        return new DefaultConfigMapApiClient(ClientBuilder.defaultClient());
    }

    @Override
    public V1ConfigMap getConfigMap(String namespace, String name) throws ApiException {
        return coreV1Api.readNamespacedConfigMap(name, namespace).execute();
    }

    @Override
    public ConfigMapWatch openWatch(String namespace, String name, String resourceVersion) throws ApiException {
        Watch<V1ConfigMap> watch = Watch.createWatch(
                apiClient,
                coreV1Api.listNamespacedConfigMap(namespace)
                        .fieldSelector("metadata.name=" + name)
                        .resourceVersion(resourceVersion)
                        .watch(true)
                        .buildCall(null),
                new TypeToken<Watch.Response<V1ConfigMap>>() {}.getType()
        );
        return new KubernetesConfigMapWatch(watch);
    }

    private static final class KubernetesConfigMapWatch implements ConfigMapWatch {

        private static final Logger LOGGER = Logger.getLogger(KubernetesConfigMapWatch.class.getName());

        private final Watch<V1ConfigMap> watch;

        private KubernetesConfigMapWatch(Watch<V1ConfigMap> watch) {
            this.watch = watch;
        }

        @Override
        public boolean hasNext() throws ApiException {
            return watch.hasNext();
        }

        @Override
        public Watch.Response<V1ConfigMap> next() throws ApiException {
            return watch.next();
        }

        @Override
        public void close() {
            try {
                watch.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to close ConfigMap watch", ex);
            }
        }

    }

}
