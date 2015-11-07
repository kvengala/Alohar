class CreateBloods < ActiveRecord::Migration
  def change
    create_table :bloods do |t|
      t.string :blood_type
      t.integer :quantity 
      t.integer :price
      t.references :parent, index: true
      t.timestamps null: false
    end
  end
end
