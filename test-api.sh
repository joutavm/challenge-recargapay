#!/bin/bash

# Wallet Service API Test Script
# This script demonstrates the API usage with curl commands

BASE_URL="http://localhost:8080/api/wallets"

echo "=== Wallet Service API Test ==="
echo ""

# Create first wallet
echo "1. Creating wallet for user1..."
USER1_ID="11111111-1111-1111-1111-111111111111"
WALLET1_RESPONSE=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER1_ID\"}")
WALLET1_ID=$(echo $WALLET1_RESPONSE | grep -o '"walletId":"[^"]*' | cut -d'"' -f4)
echo "Created wallet: $WALLET1_ID"
echo ""

# Create second wallet
echo "2. Creating wallet for user2..."
USER2_ID="22222222-2222-2222-2222-222222222222"
WALLET2_RESPONSE=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER2_ID\"}")
WALLET2_ID=$(echo $WALLET2_RESPONSE | grep -o '"walletId":"[^"]*' | cut -d'"' -f4)
echo "Created wallet: $WALLET2_ID"
echo ""

# Check balance of wallet1
echo "3. Checking balance of wallet1..."
curl -s -X GET $BASE_URL/$WALLET1_ID | jq '.'
echo ""

# Deposit to wallet1
echo "4. Depositing 100.00 to wallet1..."
curl -s -X POST $BASE_URL/$WALLET1_ID/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00}'
echo "Deposit completed"
echo ""

# Check balance after deposit
echo "5. Checking balance after deposit..."
curl -s -X GET $BASE_URL/$WALLET1_ID | jq '.'
echo ""

# Withdraw from wallet1
echo "6. Withdrawing 30.00 from wallet1..."
curl -s -X POST $BASE_URL/$WALLET1_ID/withdraw \
  -H "Content-Type: application/json" \
  -d '{"amount": 30.00}'
echo "Withdrawal completed"
echo ""

# Check balance after withdrawal
echo "7. Checking balance after withdrawal..."
curl -s -X GET $BASE_URL/$WALLET1_ID | jq '.'
echo ""

# Transfer from wallet1 to wallet2
echo "8. Transferring 25.00 from wallet1 to wallet2..."
curl -s -X POST $BASE_URL/transfer \
  -H "Content-Type: application/json" \
  -d "{\"fromWalletId\": \"$WALLET1_ID\", \"toWalletId\": \"$WALLET2_ID\", \"amount\": 25.00}"
echo "Transfer completed"
echo ""

# Check both wallets after transfer
echo "9. Checking both wallets after transfer..."
echo "Wallet1:"
curl -s -X GET $BASE_URL/$WALLET1_ID | jq '.'
echo ""
echo "Wallet2:"
curl -s -X GET $BASE_URL/$WALLET2_ID | jq '.'
echo ""

# Get wallet by user ID
echo "10. Getting wallet by user ID..."
curl -s -X GET $BASE_URL/user/$USER1_ID | jq '.'
echo ""

# Test error case - insufficient funds
echo "11. Testing error case - insufficient funds..."
curl -s -X POST $BASE_URL/$WALLET1_ID/withdraw \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000.00}' | jq '.'
echo ""

# Get historical balance (current time for demo)
echo "12. Getting historical balance..."
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
curl -s -X GET "$BASE_URL/$WALLET1_ID/history?timestamp=$TIMESTAMP" | jq '.'
echo ""

echo "=== Test completed ===" 