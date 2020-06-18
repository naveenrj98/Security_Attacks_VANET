#!/bin/sh

# Script to generate signed X509 certificates
# usage: ./gen_keys.sh <entity_name_1 entity_name_2 entity_name_3...>

CA_ALIAS="ca"
STORE_PASS="f4ncyP455WORd"
KEY_PASS="Y3tAn0th3rF4ncyPa5sW00rd"
CA_CERTIFICATE_PASS="Th1sC4antB3.0neMorePa55?"
D_NAME="CN=VanetInc,OU=KaijuBanana,O=IST,L=Lisbon,S=Lisbon,C=PT"
SUBJ="/CN=VanetInc/OU=KaijuBanana/O=IST/L=Lisbon/C=PT"
KEYS_VALIDITY=90
CA_DIR="ca/cert"
STORE_FILE="$CA_DIR/ca-keystore.jks"
CA_PEM_FILE="$CA_DIR/ca-certificate.pem.txt"
CA_KEY_FILE="$CA_DIR/ca-key.pem.txt"
VEHICLE_DIR="vehicle/cert"
RSU_DIR="rsu/cert"

# generate CA Certificate
mkdir -p "$CA_DIR/revoked"
echo -e "\033[32mGenerating the CA certificate...\033[0m"
openssl req -new -x509 -keyout $CA_KEY_FILE -out $CA_PEM_FILE -days $KEYS_VALIDITY -passout pass:$CA_CERTIFICATE_PASS -subj $SUBJ
echo -e "CA Certificate generated.\n"

# generate and sign certificates rsu and each entity
entities="rsu $*" # NOTE change here to add more RSU's
mkdir -p $VEHICLE_DIR $RSU_DIR
rm -r ${VEHICLE_DIR:?} ${RSU_DIR:?} # clean previous certificates
for entity_name in $entities
do
  [[ ! "$entity_name" = "rsu" ]] && entity_dir=$VEHICLE_DIR/$entity_name || entity_dir=$RSU_DIR/$entity_name # NOTE change here for more RSU's
  echo -e "\n\033[32mGenerating certificate and associated keystore for \"$entity_name\" at \"$entity_dir\"...\033[0m"
  mkdir -p $entity_dir
  server_kerystore_file="$entity_dir/$entity_name.jks"
  csr_file="$entity_dir/$entity_name.csr"
  keytool -keystore $server_kerystore_file -genkey -alias $entity_name -keyalg RSA -keysize 2048 -keypass $KEY_PASS -validity $KEYS_VALIDITY -storepass $STORE_PASS  -dname $D_NAME
  keytool -keystore $server_kerystore_file -certreq -alias $entity_name -keyalg rsa -file $csr_file -storepass $STORE_PASS -keypass $KEY_PASS
  openssl  x509  -req  -CA $CA_PEM_FILE -CAkey $CA_KEY_FILE -passin pass:$CA_CERTIFICATE_PASS -in $csr_file -out "$entity_dir/$entity_name.cer"  -days $KEYS_VALIDITY -CAcreateserial
  keytool -import -keystore $server_kerystore_file -file $CA_PEM_FILE  -alias $CA_ALIAS -keypass $KEY_PASS -storepass $STORE_PASS -noprompt
  keytool -import -keystore $server_kerystore_file -file "$entity_dir/$entity_name.cer" -alias $entity_name -storepass $STORE_PASS -keypass $KEY_PASS
  rm "$entity_dir/$entity_name.csr"
  echo "Copying certificate to $CA_DIR/$entity_name"
  sum=$(sha256sum $entity_dir/$entity_name.cer)
  hashed_name="${sum}_${entity_name}.cer"
  cp "$entity_dir/$entity_name.cer" "$CA_DIR/revoked/$hashed_name" # by default all certificates will be revoked, delete the ones not needed
done
