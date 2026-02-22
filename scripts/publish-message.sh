#!/bin/bash

# Script to publish a message to an SQS FIFO queue in LocalStack

# Default values
QUEUE_NAME="${QUEUE_NAME:-terraform-example-queue.fifo}"
AWS_ENDPOINT_URL="${AWS_ENDPOINT_URL:-http://host.docker.internal:4566}"
REGION="${AWS_REGION:-us-east-1}"
MESSAGE_GROUP_ID="${MESSAGE_GROUP_ID:-default}"
MESSAGE_DEDUPLICATION_ID="${MESSAGE_DEDUPLICATION_ID:-}"

echo "Publishing message to SQS FIFO queue: $QUEUE_NAME"

# Check if awslocal is available, otherwise use aws CLI with endpoint-url
if command -v awslocal &> /dev/null; then
    AWS_CMD="awslocal"
elif command -v aws &> /dev/null; then
    AWS_CMD="aws"
else
    # Use Docker AWS CLI
    AWS_CMD="docker run --rm -i -e AWS_ACCESS_KEY_ID=test -e AWS_SECRET_ACCESS_KEY=test amazon/aws-cli"
fi

# Get the queue URL
QUEUE_URL=$($AWS_CMD sqs get-queue-url \
    --queue-name "$QUEUE_NAME" \
    --endpoint-url "$AWS_ENDPOINT_URL" \
    --region "$REGION" 2>/dev/null | grep -o '"QueueUrl": "[^"]*"' | cut -d'"' -f4)

if [ -z "$QUEUE_URL" ]; then
    echo "Error: Queue $QUEUE_NAME not found. Please create it first with create-sqs-queue.sh"
    exit 1
fi

echo "Queue URL: $QUEUE_URL"

# Read message from argument or use default
MESSAGE="${1:-Hello from SQS FIFO at $(date)}"

# Build the send-message command
if [ -n "$MESSAGE_DEDUPLICATION_ID" ]; then
    # Explicit deduplication ID provided
    $AWS_CMD sqs send-message \
        --queue-url "$QUEUE_URL" \
        --message-body "$MESSAGE" \
        --message-group-id "$MESSAGE_GROUP_ID" \
        --message-deduplication-id "$MESSAGE_DEDUPLICATION_ID" \
        --endpoint-url "$AWS_ENDPOINT_URL" \
        --region "$REGION"
else
    # For FIFO queues with ContentBasedDeduplication, deduplication is automatic
    $AWS_CMD sqs send-message \
        --queue-url "$QUEUE_URL" \
        --message-body "$MESSAGE" \
        --message-group-id "$MESSAGE_GROUP_ID" \
        --endpoint-url "$AWS_ENDPOINT_URL" \
        --region "$REGION"
fi

echo "Message published successfully!"
