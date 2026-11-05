"""Exercise the real Rcon parser with controlled fragmented wire bytes."""
import struct
import unittest
from unittest.mock import patch
import server_smoke as smoke

def packet(identifier=1, kind=0, text=b'ok', tail=b'\0\0'):
    body = struct.pack('<ii', identifier, kind) + text + tail
    return struct.pack('<i', len(body)) + body

class Wire:
    def __init__(self, data):
        self.data = data
        self.requests = []
        self.closed = False
    def recv(self, count):
        self.requests.append(count)
        chunk, self.data = self.data[:min(count, 3)], self.data[min(count, 3):]
        return chunk
    def sendall(self, data):
        pass
    def settimeout(self, timeout):
        if timeout <= 0:
            raise ValueError('invalid timeout')
    def close(self):
        self.closed = True

class ProtocolChecks(unittest.TestCase):
    def client(self, data):
        c = smoke.Rcon.__new__(smoke.Rcon)
        c.sock = Wire(data)
        c.seq = 0
        return c
    def test_fragmented_valid_reply(self):
        self.assertEqual('ok', self.client(packet()).request(2, 'save-all flush'))
    def test_wrong_request_cannot_acknowledge_save(self):
        with self.assertRaises(RuntimeError):
            self.client(packet(99, text=b'Saved the game')).request(2, 'save-all flush')
    def test_wrong_type(self):
        with self.assertRaises(RuntimeError):
            self.client(packet(kind=2)).request(2, 'save-all flush')
    def test_invalid_lengths_rejected_before_body_read(self):
        for size in (-1, 0, 9, 4107, 2147483647):
            c = self.client(struct.pack('<i', size))
            with self.subTest(size=size), self.assertRaises(RuntimeError):
                c.request(2, 'save-all flush')
            self.assertEqual([4, 1], c.sock.requests)
    def test_invalid_terminators(self):
        with self.assertRaises(RuntimeError):
            self.client(packet(tail=b'xx')).request(2, 'info')
    def test_truncated_body(self):
        with self.assertRaises(RuntimeError):
            self.client(packet()[:-1]).request(2, 'info')
    def test_maximum_frame(self):
        self.assertEqual(4096, len(self.client(packet(text=b'x'*4096)).request(2, 'info')))
    def test_auth_failure(self):
        with self.assertRaisesRegex(RuntimeError, 'authentication failed'):
            self.client(packet(-1, 2)).request(3, 'password')
    def test_optional_empty_auth_preamble(self):
        self.assertEqual('', self.client(packet(kind=0, text=b'') + packet(kind=2, text=b'')).request(3, 'password'))
    def test_duplicate_preamble_rejected(self):
        with self.assertRaises(RuntimeError):
            self.client(packet(text=b'')*2).request(3, 'password')
    def test_auth_failure_closes_connection(self):
        sock = Wire(packet(-1, 2))
        with patch.object(smoke.socket, 'create_connection', return_value=sock):
            with self.assertRaises(RuntimeError):
                smoke.Rcon(1234)
        self.assertTrue(sock.closed)
    def test_whole_request_deadline(self):
        c = self.client(packet())
        with patch.object(smoke.time, 'monotonic', side_effect=[0, 0, 21, 21, 21]):
            with self.assertRaises(TimeoutError):
                c.request(2, 'info')

if __name__ == '__main__':
    unittest.main()
