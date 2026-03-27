document.addEventListener("DOMContentLoaded", () => {

    // ================= FETCH ACCOUNT =================
    async function fetchAccount() {
        try {
            const res = await fetch("/dashboard/account");

            if (!res.ok) return;

            const account = await res.json();

            if (!account || account.balance === undefined) return;

            const balanceEl = document.getElementById("actualBalance");

            if (balanceEl) {
                balanceEl.innerText = account.balance;
            }

        } catch (err) {
            console.error("Fetch Account Error:", err);
        }
    }

    // ================= DEPOSIT =================
    const depositForm = document.getElementById("depositForm");

    depositForm?.addEventListener("submit", async (e) => {
        e.preventDefault();

        const amount = parseFloat(e.target.amount.value);

        if (isNaN(amount) || amount <= 0) {
            alert("Enter valid amount");
            return;
        }

        const res = await fetch("/deposit", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `amount=${amount}`
        });

        if (!res.ok) {
            alert("Deposit failed");
            return;
        }

        await fetchAccount();
        e.target.reset();
    });

    // ================= WITHDRAW =================
    const withdrawForm = document.getElementById("withdrawForm");

    withdrawForm?.addEventListener("submit", async (e) => {
        e.preventDefault();

        const amount = parseFloat(e.target.amount.value);

        const balanceText = document.getElementById("actualBalance").innerText;
        const balance = parseFloat(balanceText.replace(/[^\d.]/g, ""));

        if (isNaN(amount) || amount <= 0) {
            alert("Enter valid amount");
            return;
        }

        if (amount > balance) {
            alert("Insufficient balance");
            return;
        }

        const remaining = balance - amount;

        if (remaining < 5000) {
            alert("Warning: Balance will go below ₹5000. Penalty ₹500 will be charged.");
        }

        const res = await fetch("/withdraw", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `amount=${amount}`
        });

        if (!res.ok) {
            alert("Withdraw failed");
            return;
        }

        await fetchAccount();
        e.target.reset();
    });

    // ================= TRANSFER =================
    const transferForm = document.getElementById("transferForm");

    transferForm?.addEventListener("submit", async (e) => {
        e.preventDefault();

        const toUsername = e.target.toUsername.value.trim();
        const amount = parseFloat(e.target.amount.value);

        if (!toUsername) {
            alert("Enter recipient username");
            return;
        }

        if (isNaN(amount) || amount <= 0) {
            alert("Enter valid amount");
            return;
        }

        const res = await fetch("/transfer", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `toUsername=${toUsername}&amount=${amount}`
        });

        if (!res.ok) {
            alert("Transfer failed");
            return;
        }

        await fetchAccount();
        e.target.reset();
    });

});