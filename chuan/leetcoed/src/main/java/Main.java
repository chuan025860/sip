
public class Main {

    public static class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    public static void main(String[] args) {
        // 创建链表 [1, 2, 4]
        ListNode list1 = new ListNode(1);       // 第一个节点值为 1
        list1.next = new ListNode(2);            // 第二个节点值为 2
        list1.next.next = new ListNode(4);       // 第三个节点值为 4

        // 打印链表的值，验证链表结构
        ListNode current = list1;
        while (current != null) {
            System.out.print(current.val + " ");
            current = current.next;
        }

    }
}