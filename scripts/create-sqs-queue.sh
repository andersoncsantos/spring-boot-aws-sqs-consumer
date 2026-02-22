#!/bin/bash

# Script to create an SQS FIFO queue in LocalStack

# Default values
QUEUE_NAME="${QUEUE_NAME:-terraform-example-queue.fifo}"
AWS_ENDPOINT_URL="${AWS_ENDPOINT_URL:-http://localhost:4566}"
REGION="${AWS_REGION:-us-east-1}"

echo "Creating SQS FIFO queue: $QUEUE_NAME"

# Check if awslocal is available, otherwise use aws CLI with endpoint-url
if command -v awslocal &> /dev/null; then
    AWS_CMD="awslocal"
elif command -v aws &> /dev/null; then
    AWS_CMD="aws"
else
    # Use Docker AWS CLI
    AWS_CMD="docker run --rm -i -e AWS_ACCESS_KEY_ID=test -e AWS_SECRET_ACCESS_KEY=test amazon/aws-cli"
fi

# Create FIFO queue with required attributes
$AWS_CMD sqs create-queue \
    --queue-name "$QUEUE_NAME" \
    --endpoint-url "$AWS_ENDPOINT_URL" \
    --region "$REGION" \
    --attributes '{"FifoQueue":"true","ContentBasedDeduplication":"true"}' 2>/dev/null || {
    # Fallback to host.docker.internal if localhost doesn't work
    echo "Retrying with host.docker.internal..."
    FALLBACK_URL="http://host.docker.internal:4566"
    $AWS_CMD sqs create-queue \
        --queue-name "$QUEUE_NAME" \
        --endpoint-url "$FALLBACK_URL" \
        --region "$REGION" \
        --attributes '{"FifoQueue":"true","ContentBasedDeduplication":"true"}'
}
