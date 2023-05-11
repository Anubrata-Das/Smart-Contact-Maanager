const toogleSidebar=()=>
{
  if($('.sidebar').is(":visible")){
      $('.sidebar').css("display","none");
      $('.content').css("margin-left","0%");
  }      
  else{
      $('.sidebar').css("display","block");
      $('.content').css("margin-left","20%");

  }
};


const search=()=>{
  let query = $("#search-input").val();
  

  if(query==""){
    $(".search-result").hide();
  }else{
    //search
    console.log(query);

    //sending request to server
    let url=`http://localhost:8282/search/${query}`;

    fetch(url)
      .then((response)=>{
        return response.json();
      })
      .then((data)=>{
        //data....
        //console.log(data);

        let text = `<div class='list-group'>`

        data.forEach((contact)=>{
          text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name}   </a>`
        });


        text+=`</div>`;

        $(".search-result").html(text);
        $(".search-result").show();
      });   
  }
};

//first request to server to create order

const paymentStart=()=>{
  let amount = $("#payment_field").val();
  if(amount=='' || amount == null){
    // alert("Amount is required !!");
    swal("Oops!", "Amount is required!", "warning");
    return;
  }
//code ajax to send request to server to create order - jquery

  $.ajax({
    url: '/user/create_order',
    data:JSON.stringify({amount:amount,info:'order_request'}),
    contentType:'application/json',
    type:'post',
    dataType:'json',
    success:function(response){
      //invoked when success
      console.log(response)
      if(response.status=='created'){
        //open payment form
        let options=
        {
          key:'rzp_test_91XGmdbt1l0HaY',
          amount:response.amount,
          currency:'INR',
          name:'Smart Contact Manager',
          description:'Donation',
          image:"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTr26hCwlhTqMGxtwcrdm0OVU_A_aWh2QJbfAC8yuT8yQ&usqp=CAU&ec=48665699",
          order_id:response.id,
          handler:function(response){
            console.log(response.razorpay_payment_id)
            console.log(response.razorpay_order_id)
            console.log(response.razorpay_signature)
            console.log("Payment successful")
            // alert("Congrats !! Payment successful")
            swal("Congrats!", "Payment successful!", "success");
          },
          prefill: {
            name: "",
            email: "",
            contact: "",
          },

          notes: {
              address: "Anubrata Das",
            },
          
          theme: {
              color: "#3399cc",
          }
        };

        let rzp = new Razorpay(options);

        rzp.on('payment.failed', function (response){
          cpnsole.log(response.error.code);
          cpnsole.log(response.error.description);
          cpnsole.log(response.error.source);
          cpnsole.log(response.error.step);
          cpnsole.log(response.error.reason);
          cpnsole.log(response.error.metadata.order_id);
          cpnsole.log(response.error.metadata.payment_id);
          // alert("Oops!! Payment failed");
          swal("Failed!", "Payment Unsuccessful!", "error");
        });

        rzp.open();
      }
    },
    error:function(error){
      //invoked when error
      console.log(error)
      alert("Something went wrong !!");
    }
  })
};