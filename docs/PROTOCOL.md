# Kramer VS-81H Protocol Notes

The VS-81H uses Kramer Protocol 2000 for classic binary control over Ethernet/TCP.

## TCP

- Default IP: `192.168.1.39`
- Default TCP port: `5000`
- Payload: raw 4-byte binary commands

## Packet format used by this app

```text
Byte 1: Instruction
Byte 2: 0x80 + input/setup value
Byte 3: 0x80 + output/value
Byte 4: 0x80 + machine number
```

## Switch video

Instruction `0x01` switches a video input to a video output.

For the VS-81H there is only one output, so output is always `0x81`.

```text
01 8X 81 81
```

`X` is input 1-8.

Example:

```text
01 85 81 81 = switch HDMI input 5 to output 1 on machine 1
```

## Disconnect output

```text
01 80 81 81
```

## Request active input

Instruction `0x05` requests the status of a video output.

```text
05 80 81 81
```

The app parses a reply like this:

```text
45 80 84 81 = output 1 is currently routed to input 4
```

## Reply handling

For valid commands with replies enabled, the switch replies with the same 4 bytes, except byte 1 has the reply/destination bit set.

Example:

```text
Sent:     01 83 81 81
Expected: 41 83 81 81
```
