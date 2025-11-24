// ---------- REGISTER ----------
document.addEventListener("DOMContentLoaded", () => {
    const registerForm = document.getElementById("registerForm");
    if(registerForm){
        registerForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const username = e.target.username.value;
            const password = e.target.password.value;

            try {
                const res = await fetch("/register", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: `username=${username}&password=${password}`
                });
                if(res.redirected){
                    window.location.href = res.url;
                } else {
                    alert("Registration failed!");
                }
            } catch (err) {
                console.error(err);
                alert("Error during registration");
            }
        });
    }

    // ---------- LOGIN ----------
    const loginForm = document.getElementById("loginForm");
    if(loginForm){
        loginForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const username = e.target.username.value;
            const password = e.target.password.value;

            try {
                const res = await fetch("/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: `username=${username}&password=${password}`
                });
                if(res.redirected){
                    window.location.href = res.url;
                } else {
                    alert("Login failed!");
                }
            } catch (err) {
                console.error(err);
                alert("Error during login");
            }
        });
    }

    // ---------- DASHBOARD ----------
   document.addEventListener("DOMContentLoaded", () => {

       const accountInfo = document.getElementById("account-info");
       const transactionsList = document.getElementById("transactions");
       const showTransactionsBtn = document.getElementById("showTransactionsBtn");

       // Fetch account info
       async function fetchAccount() {
           try {
               const res = await fetch("/dashboard/account"); // You need this endpoint returning JSON
               const account = await res.json();
               accountInfo.innerHTML = `
                   <p><strong>Username:</strong> ${account.username}</p>
                   <p><strong>Balance:</strong> $${account.balance}</p>
               `;
           } catch (err) {
               console.error(err);
               accountInfo.innerHTML = `<p>Error loading account info.</p>`;
           }
       }

       fetchAccount();

       // Deposit
       const depositForm = document.getElementById("depositForm");
       depositForm?.addEventListener("submit", async (e) => {
           e.preventDefault();
           const amount = e.target.amount.value;
           await fetch("/deposit", {
               method: "POST",
               headers: { "Content-Type": "application/x-www-form-urlencoded" },
               body: `amount=${amount}`
           });
           fetchAccount();
           e.target.reset();
       });

       // Withdraw
       const withdrawForm = document.getElementById("withdrawForm");
       withdrawForm?.addEventListener("submit", async (e) => {
           e.preventDefault();
           const amount = e.target.amount.value;
           await fetch("/withdraw", {
               method: "POST",
               headers: { "Content-Type": "application/x-www-form-urlencoded" },
               body: `amount=${amount}`
           });
           fetchAccount();
           e.target.reset();
       });

       // Transfer
       const transferForm = document.getElementById("transferForm");
       transferForm?.addEventListener("submit", async (e) => {
           e.preventDefault();
           const toUsername = e.target.toUsername.value;
           const amount = e.target.amount.value;
           await fetch("/transfer", {
               method: "POST",
               headers: { "Content-Type": "application/x-www-form-urlencoded" },
               body: `toUsername=${toUsername}&amount=${amount}`
           });
           fetchAccount();
           e.target.reset();
       });

       // Fetch transactions
       async function fetchTransactions() {
           try {
               const res = await fetch("/transaction/json"); // JSON endpoint
               const transactions = await res.json();
               transactionsList.innerHTML = "";
               transactions.forEach(t => {
                   const li = document.createElement("li");
                   const date = new Date(t.timestamp);
                   li.textContent = `${t.type} - $${t.amount} on ${date.toLocaleString()} to ${t.toAccount ? t.toAccount.username : '-'}`;
                   transactionsList.appendChild(li);
               });
           } catch (err) {
               console.error(err);
               transactionsList.innerHTML = "<li>Error loading transactions</li>";
           }
       }

       // Show transactions on button click
       showTransactionsBtn?.addEventListener("click", fetchTransactions);

   });


});
