#!/bin/bash
set -eo pipefail
FUNCTION=$(aws cloudformation describe-stack-resource --stack-name blockchainLambda --logical-resource-id function2 --query 'StackResourceDetail.PhysicalResourceId' --output text)
while true; do
  aws lambda invoke \
      --function-name $FUNCTION \
      --cli-binary-format raw-in-base64-out \
      --payload '{ "uglyXML": "<XmlBallot><event>ReplacementCon</event><election>In the Know Award Finals</election><castAt>2025-01-24T17:11:23.625911-05:00</castAt><memberUUID>9cb15cdb-3f4e-4722-852b-0c51f155cfd0</memberUUID><category><name>Best Novel</name><vote>The Integral Trees by Larry Niven</vote></category><category><name>Best Novelette</name><vote>“Winterfair Gifts” by Lois McMaster Bujold</vote><vote>“The Concrete Jungle” by Charles Stross</vote></category><category><name>Best Novella</name><vote>“Elemental” by Geoffrey A. Landis</vote></category><category><name>Best Short Story</name><vote>“Biographical Notes to ‘A Discourse on the Nature of Causality, with Air-Planes’ by Benjamin Rosenbaum”</vote></category></XmlBallot>" }' \
      out.xml
  cat out.xml
  echo ""
  sleep 2
done
