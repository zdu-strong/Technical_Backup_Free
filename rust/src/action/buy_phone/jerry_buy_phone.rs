use crate::model::phone_trait::PhoneTrait;
use crate::model::iphone_model::IPhoneModel;
use crate::model::pixel_model::PixelModel;
use futures::stream::iter;
use futures::StreamExt;

pub async fn jerry_buy_phone() {
    let ref mut phone_list = [
        PhoneTrait::IPhone(IPhoneModel {
            price: "10,000".to_string(),
            owner: "Jerry".to_string(),
        }),
        PhoneTrait::Pixel(PixelModel {
            price: "3,000".to_string(),
            owner: "Jerry".to_string(),
        }),
    ]
    .to_vec();
    iter(phone_list)
        .for_each_concurrent(10, |phone| async move {
            phone.buy().await;
        })
        .await;
}
