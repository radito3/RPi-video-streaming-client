// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("streamingclient");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("streamingclient")
//      }
//    }
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <jni.h>

/* Structure of the bytes for a DNS header */
typedef struct {
    uint16_t xid;      /* Randomly chosen identifier */
    uint16_t flags;    /* Bit-mask to indicate request/response */
    uint16_t qdcount;  /* Number of questions */
    uint16_t ancount;  /* Number of answers */
    uint16_t nscount;  /* Number of authority records */
    uint16_t arcount;  /* Number of additional records */
} dns_header_t;

/* Structure of the bytes for a DNS question */
typedef struct {
    char *name;        /* Pointer to the domain name in memory */
    uint16_t dnstype;  /* The QTYPE (1 = A) */
    uint16_t dnsclass; /* The UNICAST-RESPONSE (1 bit) and QCLASS (1 = IN) */
} dns_question_t;

/* Structure of the bytes for an IPv4 answer */
typedef struct {
    uint16_t compression;
    uint16_t type;
    uint16_t clazz;
    uint32_t ttl;
    uint16_t length;
    struct in_addr addr;
} __attribute__((packed)) dns_record_t;

void main_resolver(const char* hostname) {
    int socketfd = socket(AF_INET, SOCK_DGRAM, 0);
    struct sockaddr_in address;
    address.sin_family = AF_INET;
    /* mDNS works on IPv4 address 224.0.0.251 (0xe00000fb) */
    address.sin_addr.s_addr = htonl(0xe00000fb);
    /* mDNS runs on port 5353 */
    address.sin_port = htons(5353);

    /* Set up the DNS header */
    dns_header_t header;
    memset(&header, 0, sizeof (dns_header_t));
    header.xid = htons(0x1234);   /* Randomly chosen ID */
    header.flags = htons(0x0100); /* Q=0, RD=1 */
    header.qdcount = htons(1);    /* Sending 1 question */

    /* Set up the DNS question */
    dns_question_t question;
    question.dnstype = htons(1);  /* QTYPE 1=A */
    question.dnsclass = htons(1); /* QCLASS 1=IN */

    /* DNS name format requires two bytes more than the length of the
       domain name as a string */
    question.name = (char *) calloc(strlen(hostname) + 2, sizeof (char));
    /* Leave the first byte blank for the first field length */
    memcpy(question.name + 1, hostname, strlen(hostname));
    uint8_t *prev = (uint8_t *) question.name;
    uint8_t count = 0; /* Used to count the bytes in a field */

    /* Traverse through the name, looking for the . locations */
    for (size_t i = 0; i < strlen(hostname); i++) {
        /* A . indicates the end of a field */
        if (hostname[i] == '.') {
            /* Copy the length to the byte before this field, then
               update prev to the location of the . */
            *prev = count;
            prev = (uint8_t *) question.name + i + 1;
            count = 0;
        } else {
            count++;
        }
    }
    *prev = count;

    /* Copy all fields into a single, concatenated packet */
    size_t packetlen = sizeof (header) + strlen (hostname) + 2 +
                       sizeof (question.dnstype) + sizeof (question.dnsclass);
    uint8_t *packet = (uint8_t *) calloc(packetlen, sizeof (uint8_t));
    uint8_t *p = (uint8_t *) packet;

    /* Copy the header first */
    memcpy(p, &header, sizeof (header));
    p += sizeof (header);

    /* Copy the question name, QTYPE, and QCLASS fields */
    memcpy(p, question.name, strlen (hostname) + 1);
    p += strlen(hostname) + 2; /* includes 0 octet for end */
    memcpy(p, &question.dnstype, sizeof (question.dnstype));
    p += sizeof (question.dnstype);
    memcpy(p, &question.dnsclass, sizeof (question.dnsclass));

    /* Send the packet, then request the response */
    sendto(socketfd, packet, packetlen, 0, (struct sockaddr *) &address,
           (socklen_t) sizeof (address));

    socklen_t length = 0;
    uint8_t response[512];
    memset(&response, 0, 512);

    /* Receive the response from Avahi into a local buffer */
    ssize_t bytes = recvfrom(socketfd, response, 512, 0, (struct sockaddr *) &address, &length);
    if (bytes <= 0) {
        perror("error receiving mDNS response");
        return;
    }

    dns_header_t *response_header = (dns_header_t *) response;
    if ((ntohs(response_header->flags) & 0xf) != 0) {
        perror("error from mDNS responder");
        return;
    }

    /* Get a pointer to the start of the question name, and
       reconstruct it from the fields */
    uint8_t *start_of_name = (uint8_t *) (response + sizeof (dns_header_t));
    uint8_t total = 0;
    uint8_t *field_length = start_of_name;
    while (*field_length != 0) {
        /* Restore the dot in the name and advance to next length */
        total += *field_length + 1;
        *field_length = '.';
        field_length = start_of_name + total;
    }

    /* Skip null byte, qtype, and qclass to get to first answer */
    dns_record_t *records = (dns_record_t *) (field_length + 5);
    for (int i = 0; i < ntohs(response_header->ancount); i++) {
        //return this
        char* raspberry_address = inet_ntoa(records[i].addr);
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_tu_streamingclient_util_HostnameResolver_resolveHostname(JNIEnv *env, jobject thiz,
                                                                  jstring host) {
    const char* host_str = env->GetStringUTFChars(host, nullptr);
    printf("input arg: %s", host_str);
    return env->NewStringUTF("192.168.1.8");
}
