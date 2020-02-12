package csnight.redis.monitor.db.elastic;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ElasticRestClientAPI {
    private static Logger _log = LoggerFactory.getLogger(ElasticRestClientAPI.class);
    private ScheduledExecutorService connectCheckPool = Executors.newScheduledThreadPool(1);
    private RestHighLevelClient client;
    private JSONObject es_conf;
    private String addresses;
    private BulkProcessor bulkProcessor;
    private ScheduledFuture<?> future = null;

    public ElasticRestClientAPI(String addresses, JSONObject es_conf) {
        this.addresses = addresses;
        this.es_conf = es_conf;
        ConnectToES();
    }

    public boolean isConnected() {
        try {
            return client.ping(RequestOptions.DEFAULT);
        } catch (IOException e) {
            if (future != null) {
                return false;
            }
            future = connectCheckPool.scheduleAtFixedRate(() -> {
                if (!isConnected()) {
                    if (bulkProcessor != null) {
                        bulkProcessor.close();
                        bulkProcessor = null;
                    }
                    _log.warn("Elasticsearch server try reconnect every five seconds");
                    ConnectToES();
                } else {
                    future.cancel(false);
                    future = null;
                    if (SetIndices()) {
                        ParallelBulkInitialize();
                    }
                }
            }, 1, 5, TimeUnit.SECONDS);
            return false;
        }
    }

    public BulkProcessor getBulk() {
        return bulkProcessor;
    }

    public void ConnectToES() {
        String[] addressList = addresses.split(";");
        List<HttpHost> hosts = new ArrayList<>();
        for (String address : addressList) {
            String[] parts = address.replace("//", "").split(":");
            String server = parts[1];
            String protocol = parts[0];
            String port = parts[2];
            hosts.add(new HttpHost(server, Integer.parseInt(port), protocol));
        }
        client = new RestHighLevelClient(RestClient.builder(hosts.toArray(new HttpHost[]{})));
        if (isConnected()) {
            ParallelBulkInitialize();
        }
    }

    public boolean SetIndices() {
        if (isConnected()) {
            return es_conf.keySet().stream().map(this::createIndex).allMatch(r -> r.equals(true));
        }
        return false;
    }

    public boolean CloseES() {
        if (client != null) {
            try {
                if (!connectCheckPool.isShutdown()) {
                    connectCheckPool.shutdownNow();
                }
                CloseBulkProcessor();
                client.close();
                client = null;
                return true;
            } catch (IOException e) {
                client = null;
                return false;
            }
        }
        return true;
    }

    public boolean indexExists(String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(true);
        try {
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean createIndex(String indexName) {
        try {
            GetIndexRequest request = new GetIndexRequest(indexName);
            request.local(false);
            request.humanReadable(true);
            request.includeDefaults(true);
            if (!indexExists(indexName)) {
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
                createIndexRequest.settings(Settings.builder().put("index.number_of_shards", 1).build());
                boolean index_status = client.indices().create(createIndexRequest, RequestOptions.DEFAULT).isAcknowledged();
                boolean mapping_status = createMapping(indexName, GenerateMappingByJson(indexName));
                return index_status && mapping_status;
            } else {
                System.out.println(String.format("index:%s already exists", indexName));
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean deleteIndex(String indexName) {
        try {
            if (indexExists(indexName)) {
                DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
                AcknowledgedResponse response = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
                return response.isAcknowledged();
            } else {
                System.out.println(String.format("index:%s does not exists", indexName));
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean createMapping(String indexName, XContentBuilder mappingContent) {
        try {
            PutMappingRequest mappingRequest = new PutMappingRequest(indexName);
            mappingRequest.source(mappingContent);
            mappingRequest.source(mappingContent);
            return client.indices().putMapping(mappingRequest, RequestOptions.DEFAULT).isAcknowledged();
        } catch (Exception ex) {
            return false;
        }
    }

    private XContentBuilder GenerateMappingByJson(String indexName) {
        JSONObject jo_index = es_conf.getJSONObject(indexName);
        XContentBuilder builder = null;
        try {
            builder = XContentFactory.jsonBuilder().startObject().startObject("properties");
            for (Object k : jo_index.keySet()) {
                try {
                    builder = builder.startObject(k.toString()).field("type", jo_index.getString(k.toString())).endObject();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            builder.endObject().endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Strings.toString(Objects.requireNonNull(builder)));
        return builder;
    }

    public XContentBuilder GenerateDocByPojo(String frame, String MappingType) {
        XContentBuilder builder = null;
        try {
            builder = XContentFactory.jsonBuilder().startObject();
            JSONObject bean = JSONObject.parseObject(frame);
            JSONObject mapping = es_conf.getJSONObject(MappingType);
            for (String v : mapping.keySet()) {
                switch (mapping.getString(v)) {
                    case "keyword":
                    case "text":
                        builder.field(v, bean.getString(v) == null ? "" : bean.getString(v));
                        break;
                    case "double":
                        BigDecimal big = BigDecimal.valueOf(bean.getDouble(v)).setScale(13, RoundingMode.UP);
                        builder.field(v, big.doubleValue());
                        break;
                    case "integer":
                        builder.field(v, bean.getIntValue(v));
                        break;
                    case "long":
                        builder.field(v, bean.getLong(v));
                        break;
                    case "boolean":
                        builder.field(v, bean.getBooleanValue(v));
                        break;
                    case "date":
                        SimpleDateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        toFormat.setTimeZone((TimeZone.getDefault()));
                        builder.field(v, toFormat.format(new Date(Long.parseLong(bean.getString("tm")))));
                        break;
                    case "geo_point":
                        double x = bean.getDouble("x");
                        double y = bean.getDouble("y");
                        boolean illegal = coordinate_check(x, y);
                        x = illegal ? x : 0;
                        y = illegal ? y : 0;
                        builder.startObject(v).field("lat", y).field("lon", x).endObject();
                        break;
                    default:
                        builder.field(v, bean.getString(v) == null ? "" : bean.getString(v));
                        break;
                }
            }
            builder.endObject();
            bean = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println(Strings.toString(Objects.requireNonNull(builder)));
        return builder;
    }

    private boolean coordinate_check(double x, double y) {
        return x > 0 && x < 180 && y > 0 && y < 90;
    }

    public void ParallelBulkInitialize() {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                int numberOfActions = request.numberOfActions();
                request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                _log.info("Executing bulk [{}] with {} requests", executionId, numberOfActions);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                if (response.hasFailures()) {
                    _log.warn("Bulk [{}] executed with failures", executionId);
                } else {
                    _log.info("Bulk [{}] completed in {} milliseconds requests {}", executionId, response.getTook().getMillis(), request.numberOfActions());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                _log.error("Failed to execute bulk", failure);
            }
        };
        BulkProcessor.Builder builder = BulkProcessor.builder((request, bulkListener) ->
                client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener);
        builder.setBulkActions(500);
        builder.setConcurrentRequests(10);
        builder.setFlushInterval(TimeValue.timeValueHours(1L));
        builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(2L), 3));
        bulkProcessor = builder.build();
    }

    public boolean CloseBulkProcessor() {
        try {
            if (bulkProcessor != null) {
                bulkProcessor.flush();
                return bulkProcessor.awaitClose(2, TimeUnit.SECONDS);
            }
            return false;
        } catch (InterruptedException e) {
            bulkProcessor.close();
            return false;
        }
    }
}
