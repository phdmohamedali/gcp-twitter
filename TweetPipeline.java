/**
 * this course code is co
 * Dataflow streaming pipeline to read tweets from PubSub topic and write the payload to BigQuery
 */
public class TweetPipeline {
    private static final String TOPIC = "projects/grey-sort-challenge/topics/twitter";
    private static final String BIGQUERY_DESTINATION = "%s:twitter.tweets";

    public static void main(String[] args) {
        PipelineOptionsFactory.register(DataflowPipelineOptions.class);
        DataflowPipelineOptions options = PipelineOptionsFactory
                .fromArgs(args)
                .withValidation()
                .as(DataflowPipelineOptions.class);
        Pipeline pipeline = Pipeline.create(options);
        pipeline.apply("Tweets_Read_PubSub", PubsubIO
                .readMessagesWithAttributes()
                .fromTopic(TOPIC))
                .apply("Tweets_Extract_Payload", ParDo.of(new ExtractTweetPayload()))
                .apply("Tweets_Write_BigQuery", BigQueryIO.writeTableRows()
                        .to(String.format(BIGQUERY_DESTINATION, options.getProject()))
                        .withCreateDisposition(CREATE_IF_NEEDED)
                        .withWriteDisposition(WRITE_APPEND)
                        .withSchema(getTableSchema()));
        pipeline.run();
    }

    private static TableSchema getTableSchema() {
        List<TableFieldSchema> fields = new ArrayList<>();
        fields.add(new TableFieldSchema().setName("timestamp").setType("INTEGER"));
        fields.add(new TableFieldSchema().setName("payload").setType("STRING"));
        return new TableSchema().setFields(fields);
    }

    public static class ExtractTweetPayload extends DoFn<PubsubMessage, TableRow> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            String payload = new String(c.element().getPayload(), StandardCharsets.UTF_8);
            c.output(new TableRow()
                    .set("timestamp", System.currentTimeMillis())
                    .set("payload", payload)
            );
        }
    }
}
